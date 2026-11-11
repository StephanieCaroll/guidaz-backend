package guidaz_api.service.pedido;

import guidaz_api.dto.pedido.*;
import guidaz_api.exception.DadoInvalidoException;
import guidaz_api.exception.RegraNegocioException;
import guidaz_api.exception.RecursoNaoEncontradoException;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import guidaz_api.model.pedido.*;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.pedido.PedidoPacoteHorarioRepository;
import guidaz_api.repository.pedido.PedidoPacoteRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class PedidoPacoteService {
    private static final Integer RESERVA_ATIVA = 1;
    private static final long HORAS_PARA_CANCELAR_EM_ANALISE = 72;
    private static final Pattern ACENTOS = Pattern.compile("\\p{M}+");

    private final PedidoPacoteRepository pedidoRepository;
    private final PedidoPacoteHorarioRepository horarioRepository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final DisponibilidadeInstrutorRepository disponibilidadeRepository;
    private final BloqueioAgendaRepository bloqueioRepository;

    public PedidoPacoteService(PedidoPacoteRepository pedidoRepository,
            PedidoPacoteHorarioRepository horarioRepository, AlunoRepository alunoRepository,
            InstrutorRepository instrutorRepository,
            DisponibilidadeInstrutorRepository disponibilidadeRepository,
            BloqueioAgendaRepository bloqueioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.horarioRepository = horarioRepository;
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.bloqueioRepository = bloqueioRepository;
    }

    @Transactional(readOnly = true)
    public List<PacoteDTO> listarPacotes() {
        return PacoteCatalogo.todos().stream().map(PacoteDTO::de).toList();
    }

    @Transactional
    public PedidoPacoteDTO criar(CriarPedidoRequest request) {
        if (request == null || request.alunoId() == null || request.instrutorId() == null || request.pacoteId() == null
                || request.dataPrimeiraAula() == null || request.horaInicio() == null)
            throw new DadoInvalidoException("Informe alunoId, instrutorId, pacoteId, dataPrimeiraAula e horaInicio.");
        Aluno aluno = alunoRepository.findById(request.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        Instrutor instrutor = instrutorRepository.findById(request.instrutorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instrutor não encontrado."));
        PacoteCatalogo pacote = Optional.ofNullable(PacoteCatalogo.porId(request.pacoteId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pacote não encontrado."));
        validarCategoriaInstrutor(instrutor, pacote.getCategoria());
        List<HorarioGerado> horarios = new ArrayList<>();
        for (int semana = 0; semana < pacote.getQuantidadeAulas(); semana++) {
            horarios.add(new HorarioGerado(request.dataPrimeiraAula().plusWeeks(semana), request.horaInicio()));
        }
        Set<String> vistos = new HashSet<>();
        for (HorarioGerado h : horarios) validarHorario(instrutor, aluno, h, vistos);

        PedidoPacote pedido = new PedidoPacote();
        pedido.setAluno(aluno);
        pedido.setInstrutor(instrutor);
        pedido.setPacoteId(pacote.getId());
        pedido.setNomePacote(pacote.getNome());
        pedido.setCategoria(pacote.getCategoria());
        pedido.setQuantidadeAulas(pacote.getQuantidadeAulas());
        pedido.setPrecoTotal(pacote.getPreco());
        pedido.setStatus(StatusPedido.PENDENTE_PAGAMENTO);
        pedido.setCriadoEm(LocalDateTime.now());
        pedido = pedidoRepository.saveAndFlush(pedido);

        for (HorarioGerado h : horarios) {
            PedidoPacoteHorario slot = new PedidoPacoteHorario();
            slot.setPedido(pedido);
            slot.setAluno(aluno);
            slot.setInstrutor(instrutor);
            slot.setData(h.data());
            slot.setHoraInicio(h.horaInicio());
            slot.setHoraFim(h.horaInicio().plusHours(1));
            slot.setReservaAtiva(RESERVA_ATIVA);
            pedido.getHorarios().add(slot);
        }
        pedido = pedidoRepository.saveAndFlush(pedido);
        return PedidoPacoteDTO.de(pedido);
    }

    private void validarHorario(Instrutor instrutor, Aluno aluno, HorarioGerado h, Set<String> vistos) {
        if (h == null || h.data() == null || h.horaInicio() == null)
            throw new DadoInvalidoException("Cada horário precisa ter data e horaInicio.");
        if (h.data().isBefore(LocalDate.now())) throw new DadoInvalidoException("Não é possível selecionar uma data passada.");
        if (h.horaInicio().getMinute() != 0 || h.horaInicio().getSecond() != 0 || h.horaInicio().getNano() != 0)
            throw new DadoInvalidoException("Os horários devem começar em hora cheia, por exemplo 09:00.");
        String chave = h.data() + "T" + h.horaInicio();
        if (!vistos.add(chave)) throw new DadoInvalidoException("O pedido contém horário duplicado: " + chave);
        LocalTime fim = h.horaInicio().plusHours(1);
        DayOfWeek dia = h.data().getDayOfWeek();
        boolean dentro = disponibilidadeRepository.findByInstrutorIdAndDiaSemanaAndAtivoTrue(instrutor.getId(), dia)
                .stream().anyMatch(d -> !h.horaInicio().isBefore(d.getHoraInicio()) && !fim.isAfter(d.getHoraFim()));
        if (!dentro) throw new DadoInvalidoException("Horário fora da disponibilidade do instrutor: " + chave);
        boolean bloqueado = bloqueioRepository.findByInstrutorIdAndData(instrutor.getId(), h.data()).stream()
                .anyMatch(b -> h.horaInicio().isBefore(b.getHoraFim()) && fim.isAfter(b.getHoraInicio()));
        if (bloqueado) throw new RegraNegocioException("Horário bloqueado pelo instrutor: " + chave);
        if (horarioRepository.existsByInstrutorIdAndDataAndHoraInicioAndReservaAtiva(instrutor.getId(), h.data(), h.horaInicio(), RESERVA_ATIVA))
            throw new RegraNegocioException("Horário ocupado: " + chave);
        if (horarioRepository.existsByAlunoIdAndDataAndHoraInicioAndReservaAtiva(aluno.getId(), h.data(), h.horaInicio(), RESERVA_ATIVA))
            throw new RegraNegocioException("Você já tem uma aula nesse horário: " + chave);
    }

    private void validarCategoriaInstrutor(Instrutor instrutor, String categoriaPacote) {
        Set<String> categorias = normalizarCategorias(instrutor.getCategoriasLecionadas());
        boolean atende = "AB".equals(categoriaPacote)
                ? categorias.containsAll(Set.of("A", "B")) : categorias.contains(categoriaPacote);
        if (!atende) throw new DadoInvalidoException("O instrutor não atende à categoria " + categoriaPacote + ".");
    }

    private Set<String> normalizarCategorias(String texto) {
        if (texto == null || texto.isBlank()) return Set.of();
        String semAcentos = ACENTOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        String separado = semAcentos.replaceAll("(?i)\\s+E\\s+", ",").replace('/', ',').replace(';', ',').replace('|', ',');
        Set<String> resultado = new HashSet<>();
        for (String item : separado.split(",|\\s+")) {
            String token = item.trim().toUpperCase(Locale.ROOT);
            switch (token) {
                case "A", "MOTO" -> resultado.add("A");
                case "B", "CARRO" -> resultado.add("B");
                case "C", "CAMINHAO" -> resultado.add("C");
                case "D", "ONIBUS" -> resultado.add("D");
                case "E", "CARRETA" -> resultado.add("E");
                case "AB" -> resultado.addAll(Set.of("A", "B"));
                default -> { /* tokens não reconhecidos são ignorados */ }
            }
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public PedidoPacoteDTO buscar(Long id) { return PedidoPacoteDTO.de(obter(id)); }

    @Transactional(readOnly = true)
    public List<PedidoPacoteDTO> listarAluno(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) throw new RecursoNaoEncontradoException("Aluno não encontrado.");
        return pedidoRepository.findByAlunoIdOrderByCriadoEmDesc(alunoId).stream().map(PedidoPacoteDTO::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoPacoteDTO> listarInstrutor(Long instrutorId, StatusPedido status) {
        if (!instrutorRepository.existsById(instrutorId)) throw new RecursoNaoEncontradoException("Instrutor não encontrado.");
        List<PedidoPacote> pedidos = status == null
                ? pedidoRepository.findByInstrutorIdOrderByCriadoEmDesc(instrutorId)
                : pedidoRepository.findByInstrutorIdAndStatusOrderByCriadoEmDesc(instrutorId, status);
        return pedidos.stream().map(PedidoPacoteDTO::de).toList();
    }

    @Transactional
    public PedidoPacoteDTO informarComprovante(Long id, InformarComprovanteRequest request) {
        PedidoPacote p = obter(id);
        exigirAluno(p, request == null ? null : request.alunoId());
        exigirStatus(p, StatusPedido.PENDENTE_PAGAMENTO);
        p.setStatus(StatusPedido.EM_ANALISE);
        p.setComprovanteInformadoEm(LocalDateTime.now());
        return PedidoPacoteDTO.de(pedidoRepository.save(p));
    }

    @Transactional
    public PedidoPacoteDTO cancelar(Long id, CancelarPedidoRequest request) {
        PedidoPacote p = obter(id);
        exigirAluno(p, request == null ? null : request.alunoId());
        boolean pendente = p.getStatus() == StatusPedido.PENDENTE_PAGAMENTO;
        boolean analiseExpirada = p.getStatus() == StatusPedido.EM_ANALISE
                && p.getComprovanteInformadoEm() != null
                && !LocalDateTime.now().isBefore(p.getComprovanteInformadoEm().plusHours(HORAS_PARA_CANCELAR_EM_ANALISE));
        if (!pendente && !analiseExpirada)
            throw new RegraNegocioException("O pedido só pode ser cancelado enquanto aguarda pagamento ou após 72 horas sem análise.");
        p.setStatus(StatusPedido.CANCELADO);
        p.setCanceladoEm(LocalDateTime.now());
        liberarHorarios(p);
        return PedidoPacoteDTO.de(pedidoRepository.save(p));
    }

    @Transactional
    public PedidoPacoteDTO aprovar(Long id, AprovarPedidoRequest request) {
        PedidoPacote p = obter(id);
        exigirInstrutor(p, request == null ? null : request.instrutorId());
        exigirStatus(p, StatusPedido.EM_ANALISE);
        p.setStatus(StatusPedido.APROVADO);
        p.setAnalisadoEm(LocalDateTime.now());
        return PedidoPacoteDTO.de(pedidoRepository.save(p));
    }

    @Transactional
    public PedidoPacoteDTO rejeitar(Long id, RejeitarPedidoRequest request) {
        PedidoPacote p = obter(id);
        exigirInstrutor(p, request == null ? null : request.instrutorId());
        exigirStatus(p, StatusPedido.EM_ANALISE);
        String motivo = request == null ? null : request.motivo();
        if (motivo == null || motivo.isBlank() || motivo.trim().length() > 200)
            throw new DadoInvalidoException("Informe um motivo de rejeição com até 200 caracteres.");
        p.setStatus(StatusPedido.REJEITADO);
        p.setMotivoRejeicao(motivo.trim());
        p.setAnalisadoEm(LocalDateTime.now());
        liberarHorarios(p);
        return PedidoPacoteDTO.de(pedidoRepository.save(p));
    }

    private PedidoPacote obter(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }
    private void exigirAluno(PedidoPacote p, Long id) {
        if (id == null || !p.getAluno().getId().equals(id)) throw new RegraNegocioException("O aluno informado não é o associado a este pedido.");
    }
    private void exigirInstrutor(PedidoPacote p, Long id) {
        if (id == null || !p.getInstrutor().getId().equals(id)) throw new RegraNegocioException("O instrutor informado não é o associado a este pedido.");
    }
    private void exigirStatus(PedidoPacote p, StatusPedido esperado) {
        if (p.getStatus() != esperado) throw new RegraNegocioException("A operação não é permitida para o status " + p.getStatus() + ".");
    }
    private void liberarHorarios(PedidoPacote p) { p.getHorarios().forEach(h -> h.setReservaAtiva(null)); }

    private record HorarioGerado(LocalDate data, LocalTime horaInicio) {}
}

package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.AgendarAulaRequest;
import guidaz_api.dto.agenda.aula.AulaHistoricoDTO;
import guidaz_api.dto.agenda.aula.AvaliacaoRequest;
import guidaz_api.dto.agenda.aula.ResumoInstrutorDTO;
import guidaz_api.exception.RecursoNaoEncontradoException;
import guidaz_api.exception.RegraNegocioException;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.model.pedido.PedidoPacote;
import guidaz_api.model.pedido.PedidoPacoteHorario;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.pedido.PedidoPacoteHorarioRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import guidaz_api.service.notificacao.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AulaService {

    private static final long PRAZO_CANCELAMENTO_HORAS = 24;
    private static final long DURACAO_PADRAO_MIN = 60;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @Autowired
    private BloqueioAgendaRepository bloqueioAgendaRepository;

    @Autowired
    private DisponibilidadeInstrutorRepository disponibilidadeInstrutorRepository;

    @Autowired
    private PedidoPacoteHorarioRepository pedidoHorarioRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Transactional
    public AulaHistoricoDTO agendar(AgendarAulaRequest request) {
        if (request.getAlunoId() == null || request.getInstrutorId() == null) {
            throw new RegraNegocioException("Informe o aluno e o instrutor da aula.");
        }

        Aluno aluno = alunoRepository.findById(request.getAlunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado"));

        Instrutor instrutor = instrutorRepository.findById(request.getInstrutorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instrutor não encontrado"));

        LocalDate data = request.getDataAula();
        LocalTime inicio = request.getHoraInicio();
        if (data == null || inicio == null) {
            throw new RegraNegocioException("Informe a data e o horário de início da aula.");
        }
        LocalTime fim = request.getHoraFim() != null ? request.getHoraFim() : inicio.plusMinutes(DURACAO_PADRAO_MIN);

        boolean origemAluno = request.getPedidoPacoteId() != null;

        validarHorario(data, inicio, fim);
        validarDisponibilidadeEConflitos(
                instrutor.getId(),
                aluno.getId(),
                data,
                inicio,
                fim,
                null,
                !origemAluno,
                request.getPedidoPacoteId()
        );

        Aula aula = new Aula();
        aula.setAluno(aluno);
        aula.setInstrutor(instrutor);
        aula.setDataAula(data);
        aula.setHoraInicio(inicio);
        aula.setHoraFim(fim);
        aula.setCategoria(request.getCategoria() != null ? request.getCategoria() : "B");
        aula.setLocalEncontro(request.getLocalEncontro());
        aula.setObservacoes(request.getObservacoes());
        aula.setStatus(StatusAula.AGENDADA);

        Aula salva = aulaRepository.save(aula);

        String quando = descreverHorario(salva);
        if (origemAluno) {
            notificarInstrutor(salva, "AULA_AGENDADA", "Nova aula agendada",
                    "Um aluno agendou uma aula para " + quando + ".");
        } else {
            notificarAluno(salva, "AULA_AGENDADA", "Nova aula agendada",
                    "Seu instrutor agendou uma aula para " + quando + ".");
        }

        return AulaHistoricoDTO.fromEntity(salva, false, null);
    }

    @Transactional
    public List<AulaHistoricoDTO> gerarAulasDePacoteAprovado(PedidoPacote pedido) {
        if (pedido == null || pedido.getAluno() == null || pedido.getInstrutor() == null) {
            throw new RegraNegocioException("Dados do pedido de pacote inválidos para gerar aulas.");
        }

        List<PedidoPacoteHorario> horarios = pedido.getHorarios();
        if (horarios == null || horarios.isEmpty()) {
            throw new RegraNegocioException("O pedido aprovado não possui horários para gerar aulas.");
        }

        Aluno aluno = pedido.getAluno();
        Instrutor instrutor = pedido.getInstrutor();
        String categoria = pedido.getCategoria() != null && !pedido.getCategoria().isBlank()
                ? pedido.getCategoria().trim().toUpperCase()
                : "B";
        String observacaoPadrao = "Pacote: " + (pedido.getNomePacote() != null ? pedido.getNomePacote() : ("Categoria " + categoria));

        List<AulaHistoricoDTO> aulasCriadas = new ArrayList<>();

        for (PedidoPacoteHorario slot : horarios) {
            LocalDate data = slot.getData();
            LocalTime inicio = slot.getHoraInicio();
            LocalTime fim = slot.getHoraFim() != null
                    ? slot.getHoraFim()
                    : (inicio != null ? inicio.plusMinutes(DURACAO_PADRAO_MIN) : null);

            if (data == null || inicio == null || fim == null) {
                throw new RegraNegocioException("Horário inválido no pedido #" + pedido.getId() + ".");
            }

            validarDisponibilidadeEConflitos(
                    instrutor.getId(),
                    aluno.getId(),
                    data,
                    inicio,
                    fim,
                    null,
                    true,
                    pedido.getId()
            );

            Aula aula = new Aula();
            aula.setAluno(aluno);
            aula.setInstrutor(instrutor);
            aula.setDataAula(data);
            aula.setHoraInicio(inicio);
            aula.setHoraFim(fim);
            aula.setCategoria(categoria);
            aula.setObservacoes(observacaoPadrao);
            aula.setStatus(StatusAula.AGENDADA);

            Aula salva = aulaRepository.save(aula);
            slot.setReservaAtiva(null);
            aulasCriadas.add(AulaHistoricoDTO.fromEntity(salva, false, null));
        }

        return aulasCriadas;
    }

    @Transactional
    public AulaHistoricoDTO editarAula(Long id, AgendarAulaRequest request, String perfil) {
        Aula aula = buscarEntidade(id);
        exigirAgendada(aula, "editadas");

        LocalDate novaData = request.getDataAula() != null ? request.getDataAula() : aula.getDataAula();
        LocalTime novoInicio = request.getHoraInicio() != null ? request.getHoraInicio() : aula.getHoraInicio();
        LocalTime novoFim = request.getHoraFim() != null ? request.getHoraFim() : fimEfetivo(aula);
        if (novoInicio != null && novoFim == null) {
            novoFim = novoInicio.plusMinutes(DURACAO_PADRAO_MIN);
        }

        boolean horarioAlterado = !Objects.equals(novaData, aula.getDataAula())
                || !Objects.equals(novoInicio, aula.getHoraInicio())
                || !Objects.equals(novoFim, fimEfetivo(aula));

        if (horarioAlterado) {
            if (novaData == null || novoInicio == null || novoFim == null) {
                throw new RegraNegocioException("Informe a data e os horários de início e término da aula.");
            }
            if (ehAluno(perfil) && dentroDoPrazoCritico(aula)) {
                throw new RegraNegocioException(
                        "Reagendamentos feitos pelo aluno exigem no mínimo " + PRAZO_CANCELAMENTO_HORAS
                                + " horas de antecedência. Fale com o seu instrutor.");
            }
            validarHorario(novaData, novoInicio, novoFim);
            validarDisponibilidadeEConflitos(
                    aula.getInstrutor().getId(),
                    aula.getAluno().getId(),
                    novaData,
                    novoInicio,
                    novoFim,
                    aula.getId(),
                    false,
                    null
            );

            aula.setDataAula(novaData);
            aula.setHoraInicio(novoInicio);
            aula.setHoraFim(novoFim);
        }

        if (request.getLocalEncontro() != null) {
            aula.setLocalEncontro(request.getLocalEncontro());
        }
        if (request.getObservacoes() != null) {
            aula.setObservacoes(request.getObservacoes());
        }

        Aula atualizada = aulaRepository.save(aula);

        if (horarioAlterado) {
            String quando = descreverHorario(atualizada);
            if (ehAluno(perfil)) {
                notificarInstrutor(atualizada, "AULA_REAGENDADA", "Aula reagendada",
                        "O aluno reagendou a aula para " + quando + ".");
            } else {
                notificarAluno(atualizada, "AULA_REAGENDADA", "Aula reagendada",
                        "Seu instrutor reagendou a aula para " + quando + ".");
            }
        }

        return AulaHistoricoDTO.fromEntity(atualizada, false, null);
    }

    @Transactional(readOnly = true)
    public AulaHistoricoDTO buscarPorId(Long id, String perfil) {
        Aula aula = buscarEntidade(id);
        boolean visaoAluno = "ALUNO".equalsIgnoreCase(perfil);
        return AulaHistoricoDTO.fromEntity(aula, visaoAluno, null);
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> historicoAluno(Long alunoId) {
        return aulaRepository.findByAlunoIdOrderByDataAulaAscHoraInicioAsc(alunoId).stream()
                .map(a -> AulaHistoricoDTO.fromEntity(a, true, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> proximasAluno(Long alunoId, int limite) {
        return aulaRepository.findByAlunoIdAndStatusOrderByDataAulaAscHoraInicioAsc(alunoId, StatusAula.AGENDADA).stream()
                .limit(limite)
                .map(a -> AulaHistoricoDTO.fromEntity(a, true, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> historicoInstrutor(Long instrutorId) {
        return aulaRepository.buscarPorInstrutorComAluno(instrutorId).stream()
                .map(a -> AulaHistoricoDTO.fromEntity(a, false, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> proximasInstrutor(Long instrutorId, int limite) {
        return aulaRepository.findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(instrutorId, StatusAula.AGENDADA).stream()
                .limit(limite)
                .map(a -> AulaHistoricoDTO.fromEntity(a, false, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumoInstrutorDTO obterResumoInstrutor(Long instrutorId) {
        List<Aula> aulas = aulaRepository.buscarPorInstrutorComAluno(instrutorId);

        long totalAulas = aulas.size();
        long realizadas = aulas.stream().filter(a -> a.getStatus() == StatusAula.CONCLUIDA).count();
        long canceladas = aulas.stream().filter(a -> a.getStatus() == StatusAula.CANCELADA).count();
        long agendadas = aulas.stream().filter(a -> a.getStatus() == StatusAula.AGENDADA).count();
        long faltas = aulas.stream().filter(a -> a.getStatus() == StatusAula.FALTA_ALUNO).count();

        long minutosRealizados = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.CONCLUIDA && a.getHoraInicio() != null && a.getHoraFim() != null)
                .mapToLong(a -> Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes())
                .sum();

        long minutosPendentes = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.AGENDADA && a.getHoraInicio() != null && a.getHoraFim() != null)
                .mapToLong(a -> Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes())
                .sum();

        return ResumoInstrutorDTO.builder()
                .totalAulas(totalAulas)
                .realizadas(realizadas)
                .canceladas(canceladas)
                .faltas(faltas)
                .agendadas(agendadas)
                .horasRealizadas(minutosRealizados / 60.0)
                .horasPendentes(minutosPendentes / 60.0)
                .mediaAvaliacoes(null)
                .totalAlunos(aulas.stream().map(a -> a.getAluno().getId()).distinct().count())
                .pedidosPendentes(0)
                .build();
    }

    @Transactional
    public AulaHistoricoDTO concluirAula(Long id) {
        Aula aula = buscarEntidade(id);
        exigirAgendada(aula, "concluídas");

        LocalDateTime inicio = inicioDaAula(aula);
        if (inicio != null && LocalDateTime.now().isBefore(inicio)) {
            throw new RegraNegocioException("A aula só pode ser concluída após o horário de início.");
        }

        aula.setStatus(StatusAula.CONCLUIDA);
        Aula salva = aulaRepository.save(aula);

        notificarAluno(salva, "AULA_CONCLUIDA", "Aula concluída",
                "Sua aula de " + descreverHorario(salva) + " foi concluída. Avalie o seu instrutor!");

        return AulaHistoricoDTO.fromEntity(salva, false, null);
    }

    @Transactional
    public AulaHistoricoDTO cancelarAula(Long id, String perfil) {
        Aula aula = buscarEntidade(id);
        exigirAgendada(aula, "canceladas");

        boolean porAluno = ehAluno(perfil);
        boolean tardio = porAluno && dentroDoPrazoCritico(aula);

        aula.setStatus(tardio ? StatusAula.FALTA_ALUNO : StatusAula.CANCELADA);
        Aula salva = aulaRepository.save(aula);

        String quando = descreverHorario(salva);
        if (porAluno) {
            String complemento = tardio
                    ? " com menos de " + PRAZO_CANCELAMENTO_HORAS + "h de antecedência e foi registrada como falta."
                    : ". O horário foi liberado.";
            notificarInstrutor(salva, "AULA_CANCELADA", "Aula cancelada",
                    "O aluno cancelou a aula de " + quando + complemento);
        } else {
            notificarAluno(salva, "AULA_CANCELADA", "Aula cancelada",
                    "Seu instrutor cancelou a aula de " + quando + ". O crédito foi devolvido e o horário liberado.");
        }

        return AulaHistoricoDTO.fromEntity(salva, false, null);
    }

    public void avaliar(Long id, AvaliacaoRequest request) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));
    }

    private Aula buscarEntidade(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));
    }

    private void exigirAgendada(Aula aula, String acao) {
        if (aula.getStatus() != StatusAula.AGENDADA) {
            throw new RegraNegocioException("Apenas aulas com status Agendada podem ser " + acao + ".");
        }
    }

    private void validarHorario(LocalDate data, LocalTime inicio, LocalTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new RegraNegocioException("O horário de término deve ser posterior ao de início.");
        }
        if (LocalDateTime.of(data, inicio).isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível marcar a aula para um horário que já passou.");
        }
    }

    private void validarDisponibilidadeEConflitos(Long instrutorId, Long alunoId, LocalDate data,
                                                  LocalTime inicio, LocalTime fim, Long aulaIgnoradaId,
                                                  boolean considerarReservasDePedido, Long pedidoIgnoradoId) {
        boolean dentroDaDisponibilidade = disponibilidadeInstrutorRepository
                .findByInstrutorIdAndDiaSemanaAndAtivoTrue(instrutorId, data.getDayOfWeek()).stream()
                .anyMatch(d -> !inicio.isBefore(d.getHoraInicio()) && !fim.isAfter(d.getHoraFim()));
        if (!dentroDaDisponibilidade) {
            throw new RegraNegocioException("O instrutor não possui disponibilidade neste dia e horário.");
        }

        boolean bloqueado = bloqueioAgendaRepository.findByInstrutorIdAndData(instrutorId, data).stream()
                .anyMatch(b -> sobrepoe(inicio, fim, b.getHoraInicio(), b.getHoraFim()));
        if (bloqueado) {
            throw new RegraNegocioException("O instrutor bloqueou este horário na agenda.");
        }

        List<Aula> aulasInstrutor = aulaRepository
                .findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(instrutorId, StatusAula.AGENDADA);
        if (existeAulaSobreposta(aulasInstrutor, data, inicio, fim, aulaIgnoradaId)) {
            throw new RegraNegocioException("O instrutor já possui uma aula neste horário.");
        }

        List<Aula> aulasAluno = aulaRepository
                .findByAlunoIdAndStatusOrderByDataAulaAscHoraInicioAsc(alunoId, StatusAula.AGENDADA);
        if (existeAulaSobreposta(aulasAluno, data, inicio, fim, aulaIgnoradaId)) {
            throw new RegraNegocioException("O aluno já possui uma aula neste horário.");
        }

        if (considerarReservasDePedido) {
            List<PedidoPacoteHorario> reservas =
                    pedidoHorarioRepository.findByInstrutorIdAndDataAndReservaAtiva(instrutorId, data, 1);
            boolean reservado = reservas.stream()
                    .filter(r -> pedidoIgnoradoId == null
                            || r.getPedido() == null
                            || !Objects.equals(r.getPedido().getId(), pedidoIgnoradoId))
                    .anyMatch(r -> sobrepoe(inicio, fim, r.getHoraInicio(),
                            r.getHoraInicio().plusMinutes(DURACAO_PADRAO_MIN)));
            if (reservado) {
                throw new RegraNegocioException("Este horário está reservado por outro aluno.");
            }
        }
    }

    private boolean existeAulaSobreposta(List<Aula> aulas, LocalDate data, LocalTime inicio, LocalTime fim,
                                         Long aulaIgnoradaId) {
        return aulas.stream()
                .filter(a -> !Objects.equals(a.getId(), aulaIgnoradaId))
                .filter(a -> data.equals(a.getDataAula()) && a.getHoraInicio() != null)
                .anyMatch(a -> sobrepoe(inicio, fim, a.getHoraInicio(), fimEfetivo(a)));
    }

    private static boolean sobrepoe(LocalTime inicio1, LocalTime fim1, LocalTime inicio2, LocalTime fim2) {
        return inicio1.isBefore(fim2) && fim1.isAfter(inicio2);
    }

    private LocalTime fimEfetivo(Aula aula) {
        if (aula.getHoraFim() != null) {
            return aula.getHoraFim();
        }
        return aula.getHoraInicio() != null ? aula.getHoraInicio().plusMinutes(DURACAO_PADRAO_MIN) : null;
    }

    private LocalDateTime inicioDaAula(Aula aula) {
        if (aula.getDataAula() == null || aula.getHoraInicio() == null) {
            return null;
        }
        return LocalDateTime.of(aula.getDataAula(), aula.getHoraInicio());
    }

    private boolean dentroDoPrazoCritico(Aula aula) {
        LocalDateTime inicio = inicioDaAula(aula);
        return inicio != null && LocalDateTime.now().plusHours(PRAZO_CANCELAMENTO_HORAS).isAfter(inicio);
    }

    private boolean ehAluno(String perfil) {
        return "ALUNO".equalsIgnoreCase(perfil);
    }

    private String descreverHorario(Aula aula) {
        LocalDateTime inicio = inicioDaAula(aula);
        return inicio != null ? FORMATO_DATA_HORA.format(inicio) : "data a confirmar";
    }

    private void notificarAluno(Aula aula, String tipo, String titulo, String descricao) {
        if (aula.getAluno() != null) {
            notificacaoService.criar(aula.getAluno().getId(), "ALUNO", tipo, titulo, descricao, "AULA", aula.getId());
        }
    }

    private void notificarInstrutor(Aula aula, String tipo, String titulo, String descricao) {
        if (aula.getInstrutor() != null) {
            notificacaoService.criar(aula.getInstrutor().getId(), "INSTRUTOR", tipo, titulo, descricao, "AULA", aula.getId());
        }
    }
}
package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.AgendarAulaRequest;
import guidaz_api.dto.agenda.aula.AulaHistoricoDTO;
import guidaz_api.exception.DadoInvalidoException;
import guidaz_api.exception.RecursoNaoEncontradoException;
import guidaz_api.exception.RegraNegocioException;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.Avaliacao;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.model.aula.TipoAvaliador;
import guidaz_api.model.pedido.PedidoPacote;
import guidaz_api.model.pedido.PedidoPacoteHorario;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.aula.AvaliacaoRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import guidaz_api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AulaService {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final AulaRepository aulaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final DisponibilidadeInstrutorRepository disponibilidadeRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public AulaHistoricoDTO agendar(AgendarAulaRequest request) {
        if (request == null || request.getAlunoId() == null || request.getInstrutorId() == null) {
            throw new DadoInvalidoException("Aluno e Instrutor são obrigatórios.");
        }
        if (request.getDataAula() == null || request.getHoraInicio() == null) {
            throw new DadoInvalidoException("Data e horário de início são obrigatórios.");
        }
        if (request.getDataAula().isBefore(LocalDate.now())) {
            throw new DadoInvalidoException("Não é possível agendar uma aula em data passada.");
        }

        LocalTime horaFim = request.getHoraFim() != null
                ? request.getHoraFim()
                : request.getHoraInicio().plusHours(1);

        if (!horaFim.isAfter(request.getHoraInicio())) {
            throw new DadoInvalidoException("O horário de término deve ser posterior ao início.");
        }

        Aluno aluno = alunoRepository.findById(request.getAlunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));

        Instrutor instrutor = instrutorRepository.findById(request.getInstrutorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instrutor não encontrado."));

        DayOfWeek dia = request.getDataAula().getDayOfWeek();
        boolean dentroDisponibilidade = disponibilidadeRepository
                .findByInstrutorIdAndDiaSemanaAndAtivoTrue(instrutor.getId(), dia)
                .stream()
                .anyMatch(d -> !request.getHoraInicio().isBefore(d.getHoraInicio()) && !horaFim.isAfter(d.getHoraFim()));

        if (!dentroDisponibilidade) {
            throw new DadoInvalidoException("O horário selecionado está fora da disponibilidade do instrutor.");
        }

        boolean bloqueado = bloqueioRepository
                .findByInstrutorIdAndData(instrutor.getId(), request.getDataAula())
                .stream()
                .anyMatch(b -> request.getHoraInicio().isBefore(b.getHoraFim()) && horaFim.isAfter(b.getHoraInicio()));

        if (bloqueado) {
            throw new RegraNegocioException("Este horário está bloqueado na agenda do instrutor.");
        }

        if (aulaRepository.existeConflitoHorarioInstrutor(instrutor.getId(), request.getDataAula(), request.getHoraInicio(), horaFim)) {
            throw new RegraNegocioException("Este instrutor já possui uma aula agendada neste horário.");
        }

        if (aulaRepository.existeConflitoHorarioAluno(aluno.getId(), request.getDataAula(), request.getHoraInicio(), horaFim)) {
            throw new RegraNegocioException("O aluno já possui outra aula agendada neste horário.");
        }

        Aula aula = Aula.builder()
                .aluno(aluno)
                .instrutor(instrutor)
                .dataAula(request.getDataAula())
                .horaInicio(request.getHoraInicio())
                .horaFim(horaFim)
                .categoria(request.getCategoria() != null ? request.getCategoria() : aluno.getCategoriaPretendida())
                .localEncontro(request.getLocalEncontro())
                .observacoes(request.getObservacoes())
                .status(StatusAula.AGENDADA)
                .build();

        Aula salva = aulaRepository.save(aula);

        String quando = request.getDataAula().format(DATA_FMT) + " às " + request.getHoraInicio().format(HORA_FMT);
        notificacaoService.criar(
                aluno.getId(),
                "ALUNO",
                "agendada",
                "Nova aula agendada",
                "Sua aula com " + instrutor.getNome() + " foi marcada para " + quando + ".",
                "AULA",
                salva.getId()
        );
        notificacaoService.criar(
                instrutor.getId(),
                "INSTRUTOR",
                "agendada",
                "Nova aula na agenda",
                "O aluno " + aluno.getNome() + " agendou uma aula para " + quando + ".",
                "AULA",
                salva.getId()
        );

        return toDto(salva, true);
    }

    @Transactional
    public void gerarAulasDePacoteAprovado(PedidoPacote pedido) {
        if (pedido.getHorarios() == null || pedido.getHorarios().isEmpty()) {
            return;
        }
        for (PedidoPacoteHorario slot : pedido.getHorarios()) {
            boolean jaExiste = aulaRepository.existeConflitoHorarioInstrutor(
                    pedido.getInstrutor().getId(),
                    slot.getData(),
                    slot.getHoraInicio(),
                    slot.getHoraFim()
            );
            if (!jaExiste) {
                Aula aula = Aula.builder()
                        .aluno(pedido.getAluno())
                        .instrutor(pedido.getInstrutor())
                        .pedidoPacote(pedido)
                        .dataAula(slot.getData())
                        .horaInicio(slot.getHoraInicio())
                        .horaFim(slot.getHoraFim())
                        .categoria(pedido.getCategoria())
                        .observacoes("Aula do pacote: " + pedido.getNomePacote())
                        .status(StatusAula.AGENDADA)
                        .build();
                aulaRepository.save(aula);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> listarPorAluno(Long alunoId) {
        return aulaRepository.findByAlunoIdOrderByDataAulaAscHoraInicioAsc(alunoId)
                .stream()
                .map(a -> toDto(a, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> listarProximasAluno(Long alunoId, int limite) {
        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        return aulaRepository.findByAlunoIdAndStatusOrderByDataAulaAscHoraInicioAsc(alunoId, StatusAula.AGENDADA)
                .stream()
                .filter(a -> a.getDataAula().isAfter(hoje) || (a.getDataAula().isEqual(hoje) && !a.getHoraFim().isBefore(agora)))
                .limit(limite)
                .map(a -> toDto(a, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> listarPorInstrutor(Long instrutorId) {
        return aulaRepository.findByInstrutorIdOrderByDataAulaAscHoraInicioAsc(instrutorId)
                .stream()
                .map(a -> toDto(a, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AulaHistoricoDTO> listarProximasInstrutor(Long instrutorId, int limite) {
        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        return aulaRepository.findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(instrutorId, StatusAula.AGENDADA)
                .stream()
                .filter(a -> a.getDataAula().isAfter(hoje) || (a.getDataAula().isEqual(hoje) && !a.getHoraFim().isBefore(agora)))
                .limit(limite)
                .map(a -> toDto(a, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public AulaHistoricoDTO buscarPorId(Long id, boolean visaoAluno) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada."));
        return toDto(aula, visaoAluno);
    }

    @Transactional
    public AulaHistoricoDTO concluir(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada."));
        aula.setStatus(StatusAula.CONCLUIDA);
        Aula salva = aulaRepository.save(aula);

        String quando = aula.getDataAula().format(DATA_FMT) + " às " + aula.getHoraInicio().format(HORA_FMT);
        notificacaoService.criar(
                aula.getAluno().getId(),
                "ALUNO",
                "pagamento",
                "Aula concluída!",
                "Sua aula de " + quando + " com " + aula.getInstrutor().getNome() + " foi concluída. Toque para avaliar!",
                "AULA",
                salva.getId()
        );

        return toDto(salva, false);
    }

    @Transactional
    public AulaHistoricoDTO cancelar(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada."));
        if (aula.getStatus() == StatusAula.CONCLUIDA) {
            throw new RegraNegocioException("Não é possível cancelar uma aula já concluída.");
        }
        aula.setStatus(StatusAula.CANCELADA);
        Aula salva = aulaRepository.save(aula);

        String quando = aula.getDataAula().format(DATA_FMT) + " às " + aula.getHoraInicio().format(HORA_FMT);
        notificacaoService.criar(
                aula.getAluno().getId(),
                "ALUNO",
                "cancelada",
                "Aula cancelada",
                "A aula marcada para " + quando + " com " + aula.getInstrutor().getNome() + " foi cancelada.",
                "AULA",
                salva.getId()
        );
        notificacaoService.criar(
                aula.getInstrutor().getId(),
                "INSTRUTOR",
                "cancelada",
                "Aula cancelada",
                "A aula marcada para " + quando + " com " + aula.getAluno().getNome() + " foi cancelada.",
                "AULA",
                salva.getId()
        );

        return toDto(salva, true);
    }

    private AulaHistoricoDTO toDto(Aula aula, boolean visaoAluno) {
        TipoAvaliador tipo = visaoAluno ? TipoAvaliador.ALUNO : TipoAvaliador.INSTRUTOR;
        Integer nota = avaliacaoRepository.findByAulaIdAndTipoAvaliador(aula.getId(), tipo)
                .map(Avaliacao::getNota)
                .orElse(null);
        return AulaHistoricoDTO.fromEntity(aula, visaoAluno, nota);
    }
}
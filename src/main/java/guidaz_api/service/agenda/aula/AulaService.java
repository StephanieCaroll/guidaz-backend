package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.AgendarAulaRequest;
import guidaz_api.dto.agenda.aula.AulaHistoricoDTO;
import guidaz_api.dto.agenda.aula.AvaliacaoRequest;
import guidaz_api.dto.agenda.aula.ResumoInstrutorDTO;
import guidaz_api.exception.RecursoNaoEncontradoException;
import guidaz_api.exception.RegraNegocioException;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AulaService {

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

    public AulaHistoricoDTO agendar(AgendarAulaRequest request) {
        Aluno aluno = alunoRepository.findById(request.getAlunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado"));

        Instrutor instrutor = instrutorRepository.findById(request.getInstrutorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instrutor não encontrado"));

        LocalDate data = request.getDataAula();
        LocalTime inicio = request.getHoraInicio();
        LocalTime fim = request.getHoraFim() != null ? request.getHoraFim() : (inicio != null ? inicio.plusHours(1) : null);

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
        return AulaHistoricoDTO.fromEntity(salva, false, null);
    }

    public AulaHistoricoDTO editarAula(Long id, AgendarAulaRequest request) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));

        if (aula.getStatus() != StatusAula.AGENDADA) {
            throw new RegraNegocioException("Apenas aulas com status Agendada podem ser editadas.");
        }

        if (request.getDataAula() != null) {
            aula.setDataAula(request.getDataAula());
        }
        if (request.getHoraInicio() != null) {
            aula.setHoraInicio(request.getHoraInicio());
        }
        if (request.getHoraFim() != null) {
            aula.setHoraFim(request.getHoraFim());
        }
        if (request.getLocalEncontro() != null) {
            aula.setLocalEncontro(request.getLocalEncontro());
        }
        if (request.getObservacoes() != null) {
            aula.setObservacoes(request.getObservacoes());
        }

        Aula atualizada = aulaRepository.save(aula);
        return AulaHistoricoDTO.fromEntity(atualizada, false, null);
    }

    public AulaHistoricoDTO buscarPorId(Long id, String perfil) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));
        boolean visaoAluno = "ALUNO".equalsIgnoreCase(perfil);
        return AulaHistoricoDTO.fromEntity(aula, visaoAluno, null);
    }

    public List<AulaHistoricoDTO> historicoAluno(Long alunoId) {
        return aulaRepository.findByAlunoIdOrderByDataAulaAscHoraInicioAsc(alunoId).stream()
                .map(a -> AulaHistoricoDTO.fromEntity(a, true, null))
                .collect(Collectors.toList());
    }

    public List<AulaHistoricoDTO> proximasAluno(Long alunoId, int limite) {
        return aulaRepository.findByAlunoIdAndStatusOrderByDataAulaAscHoraInicioAsc(alunoId, StatusAula.AGENDADA).stream()
                .limit(limite)
                .map(a -> AulaHistoricoDTO.fromEntity(a, true, null))
                .collect(Collectors.toList());
    }

    public List<AulaHistoricoDTO> historicoInstrutor(Long instrutorId) {
        return aulaRepository.buscarPorInstrutorComAluno(instrutorId).stream()
                .map(a -> AulaHistoricoDTO.fromEntity(a, false, null))
                .collect(Collectors.toList());
    }

    public List<AulaHistoricoDTO> proximasInstrutor(Long instrutorId, int limite) {
        return aulaRepository.findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(instrutorId, StatusAula.AGENDADA).stream()
                .limit(limite)
                .map(a -> AulaHistoricoDTO.fromEntity(a, false, null))
                .collect(Collectors.toList());
    }

    public ResumoInstrutorDTO obterResumoInstrutor(Long instrutorId) {
        List<Aula> aulas = aulaRepository.buscarPorInstrutorComAluno(instrutorId);

        long totalAulas = aulas.size();
        long realizadas = aulas.stream().filter(a -> a.getStatus() == StatusAula.CONCLUIDA).count();
        long canceladas = aulas.stream().filter(a -> a.getStatus() == StatusAula.CANCELADA).count();
        long agendadas = aulas.stream().filter(a -> a.getStatus() == StatusAula.AGENDADA).count();
        long faltas = 0;

        long minutosRealizados = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.CONCLUIDA && a.getHoraInicio() != null && a.getHoraFim() != null)
                .mapToLong(a -> java.time.Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes())
                .sum();

        long minutosPendentes = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.AGENDADA && a.getHoraInicio() != null && a.getHoraFim() != null)
                .mapToLong(a -> java.time.Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes())
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

    public AulaHistoricoDTO concluirAula(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));
        aula.setStatus(StatusAula.CONCLUIDA);
        return AulaHistoricoDTO.fromEntity(aulaRepository.save(aula), false, null);
    }

    public AulaHistoricoDTO cancelarAula(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));
        aula.setStatus(StatusAula.CANCELADA);
        return AulaHistoricoDTO.fromEntity(aulaRepository.save(aula), false, null);
    }

    public void avaliar(Long id, AvaliacaoRequest request) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));
    }
}
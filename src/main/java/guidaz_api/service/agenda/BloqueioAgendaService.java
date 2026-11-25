package guidaz_api.service.agenda;

import guidaz_api.dto.agenda.BloqueioAgendaDTO;
import guidaz_api.exception.RecursoNaoEncontradoException;
import guidaz_api.exception.RegraNegocioException;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BloqueioAgendaService {

    private final BloqueioAgendaRepository bloqueioRepository;
    private final InstrutorRepository instrutorRepository;
    private final AulaRepository aulaRepository;

    public BloqueioAgendaService(BloqueioAgendaRepository bloqueioRepository,
                                 InstrutorRepository instrutorRepository,
                                 AulaRepository aulaRepository) {
        this.bloqueioRepository = bloqueioRepository;
        this.instrutorRepository = instrutorRepository;
        this.aulaRepository = aulaRepository;
    }

    @Transactional(readOnly = true)
    public List<BloqueioAgendaDTO> buscarPorInstrutor(Long instrutorId) {
        return bloqueioRepository.findByInstrutorId(instrutorId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public BloqueioAgendaDTO criarBloqueio(Long instrutorId, BloqueioAgendaDTO dto) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instrutor não encontrado com ID: " + instrutorId));

        validarPeriodo(dto);
        validarConflitos(instrutorId, dto);

        BloqueioAgenda entity = new BloqueioAgenda();
        entity.setInstrutor(instrutor);
        entity.setData(dto.getData());
        entity.setHoraInicio(dto.getHoraInicio());
        entity.setHoraFim(dto.getHoraFim());
        entity.setMotivo(dto.getMotivo());

        BloqueioAgenda salvo = bloqueioRepository.save(entity);
        return toDTO(salvo);
    }

    @Transactional
    public void removerBloqueio(Long bloqueioId) {
        if (!bloqueioRepository.existsById(bloqueioId)) {
            throw new RecursoNaoEncontradoException("Bloqueio não encontrado.");
        }
        bloqueioRepository.deleteById(bloqueioId);
    }

    private void validarPeriodo(BloqueioAgendaDTO dto) {
        if (dto.getData() == null || dto.getHoraInicio() == null || dto.getHoraFim() == null) {
            throw new RegraNegocioException("Informe a data, a hora de início e a hora de término do bloqueio.");
        }
        if (!dto.getHoraFim().isAfter(dto.getHoraInicio())) {
            throw new RegraNegocioException("A hora de início do bloqueio deve ser anterior à hora de término.");
        }
        if (LocalDateTime.of(dto.getData(), dto.getHoraFim()).isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível bloquear um período que já passou.");
        }
    }

    private void validarConflitos(Long instrutorId, BloqueioAgendaDTO dto) {
        boolean conflitoComBloqueio = bloqueioRepository.findByInstrutorIdAndData(instrutorId, dto.getData()).stream()
                .anyMatch(b -> sobrepoe(dto.getHoraInicio(), dto.getHoraFim(), b.getHoraInicio(), b.getHoraFim()));
        if (conflitoComBloqueio) {
            throw new RegraNegocioException("Já existe um bloqueio que conflita com este período.");
        }

        boolean conflitoComAula = aulaRepository
                .findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(instrutorId, StatusAula.AGENDADA).stream()
                .filter(a -> dto.getData().equals(a.getDataAula()) && a.getHoraInicio() != null)
                .anyMatch(a -> {
                    LocalTime fimAula = a.getHoraFim() != null ? a.getHoraFim() : a.getHoraInicio().plusHours(1);
                    return sobrepoe(dto.getHoraInicio(), dto.getHoraFim(), a.getHoraInicio(), fimAula);
                });
        if (conflitoComAula) {
            throw new RegraNegocioException(
                    "Existe uma aula agendada neste período. Cancele ou reagende a aula antes de bloquear o horário.");
        }
    }

    private static boolean sobrepoe(LocalTime inicio1, LocalTime fim1, LocalTime inicio2, LocalTime fim2) {
        return inicio1.isBefore(fim2) && fim1.isAfter(inicio2);
    }

    private BloqueioAgendaDTO toDTO(BloqueioAgenda entity) {
        return new BloqueioAgendaDTO(
                entity.getId(),
                entity.getInstrutor() != null ? entity.getInstrutor().getId() : null,
                entity.getData(),
                entity.getHoraInicio(),
                entity.getHoraFim(),
                entity.getMotivo()
        );
    }
}
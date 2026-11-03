package guidaz_api.service.agenda;

import guidaz_api.dto.agenda.BloqueioAgendaDTO;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BloqueioAgendaService {

    private final BloqueioAgendaRepository bloqueioRepository;
    private final InstrutorRepository instrutorRepository;

    public BloqueioAgendaService(BloqueioAgendaRepository bloqueioRepository, InstrutorRepository instrutorRepository) {
        this.bloqueioRepository = bloqueioRepository;
        this.instrutorRepository = instrutorRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com ID: " + instrutorId));

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
        bloqueioRepository.deleteById(bloqueioId);
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
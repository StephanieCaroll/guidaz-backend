package guidaz_api.service.agenda;

import guidaz_api.dto.agenda.DisponibilidadeInstrutorDTO;
import guidaz_api.dto.agenda.SlotDisponivelDTO;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadeInstrutorService {

    private final DisponibilidadeInstrutorRepository disponibilidadeRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final InstrutorRepository instrutorRepository;

    public DisponibilidadeInstrutorService(
            DisponibilidadeInstrutorRepository disponibilidadeRepository,
            BloqueioAgendaRepository bloqueioRepository,
            InstrutorRepository instrutorRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.instrutorRepository = instrutorRepository;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeInstrutorDTO> buscarPorInstrutor(Long instrutorId) {
        return disponibilidadeRepository.findByInstrutorId(instrutorId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public List<DisponibilidadeInstrutorDTO> salvarDisponibilidades(Long instrutorId, List<DisponibilidadeInstrutorDTO> dtos) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com o ID: " + instrutorId));

        disponibilidadeRepository.deleteByInstrutorId(instrutorId);

        List<DisponibilidadeInstrutor> disponibilidades = dtos.stream().map(dto -> {
            DisponibilidadeInstrutor entity = new DisponibilidadeInstrutor();
            entity.setInstrutor(instrutor);
            entity.setDiaSemana(dto.getDiaSemana());
            entity.setHoraInicio(dto.getHoraInicio());
            entity.setHoraFim(dto.getHoraFim());
            entity.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
            return entity;
        }).toList();

        List<DisponibilidadeInstrutor> salvas = disponibilidadeRepository.saveAll(disponibilidades);
        return salvas.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SlotDisponivelDTO> gerarSlotsDoDia(Long instrutorId, LocalDate data) {
        DayOfWeek diaSemana = data.getDayOfWeek();
        List<DisponibilidadeInstrutor> disponibilidadesDia = disponibilidadeRepository
                .findByInstrutorIdAndDiaSemanaAndAtivoTrue(instrutorId, diaSemana);
        List<BloqueioAgenda> bloqueios = bloqueioRepository.findByInstrutorIdAndData(instrutorId, data);

        List<SlotDisponivelDTO> slots = new ArrayList<>();

        for (DisponibilidadeInstrutor disp : disponibilidadesDia) {
            LocalTime atual = disp.getHoraInicio();
            while (atual.isBefore(disp.getHoraFim())) {
                final LocalTime horaSlot = atual;
                boolean bloqueado = bloqueios.stream().anyMatch(b ->
                        !horaSlot.isBefore(b.getHoraInicio()) && horaSlot.isBefore(b.getHoraFim())
                );

                slots.add(new SlotDisponivelDTO(
                        horaSlot,
                        !bloqueado,
                        bloqueado ? "BLOQUEADO_PESSOAL" : "LIVRE"
                ));
                atual = atual.plusHours(1);
            }
        }

        return slots;
    }

    private DisponibilidadeInstrutorDTO toDTO(DisponibilidadeInstrutor entity) {
        return new DisponibilidadeInstrutorDTO(
                entity.getId(),
                entity.getInstrutor() != null ? entity.getInstrutor().getId() : null,
                entity.getDiaSemana(),
                entity.getHoraInicio(),
                entity.getHoraFim(),
                entity.getAtivo()
        );
    }
}
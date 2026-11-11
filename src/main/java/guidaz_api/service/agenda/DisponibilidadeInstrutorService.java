package guidaz_api.service.agenda;

import guidaz_api.dto.agenda.DisponibilidadeInstrutorDTO;
import guidaz_api.dto.agenda.SlotDisponivelDTO;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import guidaz_api.model.pedido.PedidoPacoteHorario;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.pedido.PedidoPacoteHorarioRepository;
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
    private final PedidoPacoteHorarioRepository pedidoHorarioRepository;

    public DisponibilidadeInstrutorService(DisponibilidadeInstrutorRepository disponibilidadeRepository,
            BloqueioAgendaRepository bloqueioRepository, InstrutorRepository instrutorRepository,
            PedidoPacoteHorarioRepository pedidoHorarioRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.instrutorRepository = instrutorRepository;
        this.pedidoHorarioRepository = pedidoHorarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeInstrutorDTO> buscarPorInstrutor(Long instrutorId) {
        return disponibilidadeRepository.findByInstrutorId(instrutorId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public List<DisponibilidadeInstrutorDTO> salvarDisponibilidades(Long instrutorId, List<DisponibilidadeInstrutorDTO> dtos) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com o ID: " + instrutorId));
        disponibilidadeRepository.deleteByInstrutorId(instrutorId);
        List<DisponibilidadeInstrutor> entities = dtos.stream().map(dto -> {
            DisponibilidadeInstrutor e = new DisponibilidadeInstrutor();
            e.setInstrutor(instrutor); e.setDiaSemana(dto.getDiaSemana());
            e.setHoraInicio(dto.getHoraInicio()); e.setHoraFim(dto.getHoraFim());
            e.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
            return e;
        }).toList();
        return disponibilidadeRepository.saveAll(entities).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SlotDisponivelDTO> gerarSlotsDoDia(Long instrutorId, LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        List<DisponibilidadeInstrutor> disponibilidades = disponibilidadeRepository.findByInstrutorIdAndDiaSemanaAndAtivoTrue(instrutorId, dia);
        List<BloqueioAgenda> bloqueios = bloqueioRepository.findByInstrutorIdAndData(instrutorId, data);
        List<PedidoPacoteHorario> reservas = pedidoHorarioRepository.findByInstrutorIdAndDataAndReservaAtiva(instrutorId, data, 1);
        List<SlotDisponivelDTO> slots = new ArrayList<>();
        for (DisponibilidadeInstrutor disp : disponibilidades) {
            LocalTime hora = disp.getHoraInicio();
            while (hora.isBefore(disp.getHoraFim())) {
                final LocalTime slot = hora;
                boolean bloqueado = bloqueios.stream().anyMatch(b -> !slot.isBefore(b.getHoraInicio()) && slot.isBefore(b.getHoraFim()));
                boolean reservado = reservas.stream().anyMatch(r -> r.getHoraInicio().equals(slot));
                String status = bloqueado ? "BLOQUEADO_PESSOAL" : reservado ? "OCUPADO" : "LIVRE";
                boolean disponivel = !bloqueado && !reservado;
                slots.add(new SlotDisponivelDTO(slot, disponivel, status));
                hora = hora.plusHours(1);
            }
        }
        return slots;
    }

    private DisponibilidadeInstrutorDTO toDTO(DisponibilidadeInstrutor entity) {
        return new DisponibilidadeInstrutorDTO(entity.getId(), entity.getInstrutor() != null ? entity.getInstrutor().getId() : null,
                entity.getDiaSemana(), entity.getHoraInicio(), entity.getHoraFim(), entity.getAtivo());
    }
}

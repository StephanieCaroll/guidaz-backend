package guidaz_api.service.agenda;

import guidaz_api.dto.agenda.DisponibilidadeInstrutorDTO;
import guidaz_api.dto.agenda.SlotDisponivelDTO;
import guidaz_api.exception.RegraNegocioException;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.model.pedido.PedidoPacoteHorario;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.pedido.PedidoPacoteHorarioRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class DisponibilidadeInstrutorService {

    private static final int DURACAO_SLOT_MIN = 60;

    private final DisponibilidadeInstrutorRepository disponibilidadeRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final InstrutorRepository instrutorRepository;
    private final PedidoPacoteHorarioRepository pedidoHorarioRepository;
    private final AulaRepository aulaRepository;

    public DisponibilidadeInstrutorService(DisponibilidadeInstrutorRepository disponibilidadeRepository,
            BloqueioAgendaRepository bloqueioRepository, InstrutorRepository instrutorRepository,
            PedidoPacoteHorarioRepository pedidoHorarioRepository, AulaRepository aulaRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.instrutorRepository = instrutorRepository;
        this.pedidoHorarioRepository = pedidoHorarioRepository;
        this.aulaRepository = aulaRepository;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeInstrutorDTO> buscarPorInstrutor(Long instrutorId) {
        return disponibilidadeRepository.findByInstrutorId(instrutorId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public List<DisponibilidadeInstrutorDTO> salvarDisponibilidades(Long instrutorId, List<DisponibilidadeInstrutorDTO> dtos) {
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com o ID: " + instrutorId));

        List<DisponibilidadeInstrutorDTO> lista = dtos != null ? dtos : List.of();
        validarDisponibilidades(lista);

        disponibilidadeRepository.deleteByInstrutorId(instrutorId);
        List<DisponibilidadeInstrutor> entities = lista.stream().map(dto -> {
            DisponibilidadeInstrutor e = new DisponibilidadeInstrutor();
            e.setInstrutor(instrutor);
            e.setDiaSemana(dto.getDiaSemana());
            e.setHoraInicio(dto.getHoraInicio());
            e.setHoraFim(dto.getHoraFim());
            e.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
            return e;
        }).toList();
        return disponibilidadeRepository.saveAll(entities).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SlotDisponivelDTO> gerarSlotsDoDia(Long instrutorId, LocalDate data) {
        if (data == null) {
            throw new RegraNegocioException("Informe a data para consultar os horários.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();
        if (data.isBefore(hoje)) {
            return List.of();
        }

        DayOfWeek dia = data.getDayOfWeek();
        List<DisponibilidadeInstrutor> disponibilidades =
                disponibilidadeRepository.findByInstrutorIdAndDiaSemanaAndAtivoTrue(instrutorId, dia);
        List<BloqueioAgenda> bloqueios = bloqueioRepository.findByInstrutorIdAndData(instrutorId, data);
        List<PedidoPacoteHorario> reservas =
                pedidoHorarioRepository.findByInstrutorIdAndDataAndReservaAtiva(instrutorId, data, 1);
        List<Aula> aulas = aulaRepository
                .findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(instrutorId, StatusAula.AGENDADA).stream()
                .filter(a -> data.equals(a.getDataAula()) && a.getHoraInicio() != null)
                .toList();

        Map<LocalTime, SlotDisponivelDTO> slots = new TreeMap<>();

        for (DisponibilidadeInstrutor disp : disponibilidades) {
            int inicioMin = disp.getHoraInicio().toSecondOfDay() / 60;
            int fimMin = disp.getHoraFim().toSecondOfDay() / 60;

            for (int min = inicioMin; min + DURACAO_SLOT_MIN <= fimMin; min += DURACAO_SLOT_MIN) {
                LocalTime inicio = LocalTime.of(min / 60, min % 60);
                if (slots.containsKey(inicio)) {
                    continue;
                }
                LocalTime fim = inicio.plusMinutes(DURACAO_SLOT_MIN);

                boolean passado = data.isEqual(hoje) && !inicio.isAfter(agora.toLocalTime());
                boolean bloqueado = bloqueios.stream()
                        .anyMatch(b -> sobrepoe(inicio, fim, b.getHoraInicio(), b.getHoraFim()));
                boolean comAula = aulas.stream()
                        .anyMatch(a -> sobrepoe(inicio, fim, a.getHoraInicio(), fimDaAula(a)));
                boolean reservado = reservas.stream()
                        .anyMatch(r -> sobrepoe(inicio, fim, r.getHoraInicio(),
                                r.getHoraInicio().plusMinutes(DURACAO_SLOT_MIN)));

                String status;
                if (passado) {
                    status = "PASSADO";
                } else if (bloqueado) {
                    status = "BLOQUEADO_PESSOAL";
                } else if (comAula) {
                    status = "OCUPADO_AULA";
                } else if (reservado) {
                    status = "OCUPADO";
                } else {
                    status = "LIVRE";
                }

                slots.put(inicio, new SlotDisponivelDTO(inicio, "LIVRE".equals(status), status));
            }
        }
        return new ArrayList<>(slots.values());
    }

    private void validarDisponibilidades(List<DisponibilidadeInstrutorDTO> dtos) {
        for (DisponibilidadeInstrutorDTO dto : dtos) {
            if (dto.getDiaSemana() == null || dto.getHoraInicio() == null || dto.getHoraFim() == null) {
                throw new RegraNegocioException("Dia da semana, hora de início e hora de término são obrigatórios.");
            }
            if (!dto.getHoraFim().isAfter(dto.getHoraInicio())) {
                throw new RegraNegocioException("A hora de início deve ser anterior à hora de término.");
            }
        }

        for (int i = 0; i < dtos.size(); i++) {
            for (int j = i + 1; j < dtos.size(); j++) {
                DisponibilidadeInstrutorDTO a = dtos.get(i);
                DisponibilidadeInstrutorDTO b = dtos.get(j);
                if (estaAtivo(a) && estaAtivo(b)
                        && Objects.equals(a.getDiaSemana(), b.getDiaSemana())
                        && sobrepoe(a.getHoraInicio(), a.getHoraFim(), b.getHoraInicio(), b.getHoraFim())) {
                    throw new RegraNegocioException("Existem horários ativos sobrepostos no mesmo dia da semana.");
                }
            }
        }
    }

    private boolean estaAtivo(DisponibilidadeInstrutorDTO dto) {
        return dto.getAtivo() == null || dto.getAtivo();
    }

    private static boolean sobrepoe(LocalTime inicio1, LocalTime fim1, LocalTime inicio2, LocalTime fim2) {
        return inicio1.isBefore(fim2) && fim1.isAfter(inicio2);
    }

    private static LocalTime fimDaAula(Aula aula) {
        return aula.getHoraFim() != null ? aula.getHoraFim() : aula.getHoraInicio().plusMinutes(DURACAO_SLOT_MIN);
    }

    private DisponibilidadeInstrutorDTO toDTO(DisponibilidadeInstrutor entity) {
        return new DisponibilidadeInstrutorDTO(entity.getId(), entity.getInstrutor() != null ? entity.getInstrutor().getId() : null,
                entity.getDiaSemana(), entity.getHoraInicio(), entity.getHoraFim(), entity.getAtivo());
    }
}
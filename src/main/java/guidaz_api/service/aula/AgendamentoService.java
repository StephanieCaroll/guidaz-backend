package guidaz_api.service.aula;

import guidaz_api.dto.agenda.SlotDisponivelDTO;
import guidaz_api.model.aula.Agendamento;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.aula.AgendamentoRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import guidaz_api.service.agenda.DisponibilidadeInstrutorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Reúne as operações para consultar horários e reservar aulas. */
@Service
public class AgendamentoService {

    /** Persiste e consulta os agendamentos existentes. */
    private final AgendamentoRepository agendamentoRepository;

    /** Consulta os horários disponibilizados e bloqueados pelo instrutor. */
    private final DisponibilidadeInstrutorService disponibilidadeService;

    /** Localiza o aluno que está solicitando a reserva. */
    private final AlunoRepository alunoRepository;

    /** Localiza o instrutor escolhido para a aula. */
    private final InstrutorRepository instrutorRepository;

    /** Recebe as dependências usadas para consultar e salvar agendamentos. */
    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            DisponibilidadeInstrutorService disponibilidadeService,
            AlunoRepository alunoRepository,
            InstrutorRepository instrutorRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.disponibilidadeService = disponibilidadeService;
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
    }

    /** Retorna os horários livres do instrutor, removendo os que já foram reservados. */
    @Transactional(readOnly = true)
    public List<SlotDisponivelDTO> buscarHorariosDisponiveis(Long instrutorId, LocalDate data) {
        List<Agendamento> agendamentos = agendamentoRepository.findByInstrutorIdAndData(instrutorId, data);

        return disponibilidadeService.gerarSlotsDoDia(instrutorId, data).stream()
                .filter(slot -> Boolean.TRUE.equals(slot.getDisponivel()))
                .filter(slot -> agendamentos.stream().noneMatch(agendamento ->
                        sobrepoe(
                                slot.getHora(),
                                slot.getHora().plusHours(1),
                                agendamento
                        )))
                .toList();
    }

    /** Reserva um horário disponível para o aluno, considerando uma aula de uma hora. */
    @Transactional
    public Agendamento criarAgendamento(Long alunoId, Long instrutorId, LocalDate data, LocalTime horaInicio) {
        boolean horarioDisponivel = buscarHorariosDisponiveis(instrutorId, data).stream()
                .anyMatch(slot -> slot.getHora().equals(horaInicio));

        if (!horarioDisponivel) {
            throw new IllegalArgumentException("O horário selecionado não está disponível.");
        }

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado com o ID: " + alunoId));
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new IllegalArgumentException("Instrutor não encontrado com o ID: " + instrutorId));

        Agendamento agendamento = new Agendamento();
        agendamento.setAluno(aluno);
        agendamento.setInstrutor(instrutor);
        agendamento.setData(data);
        agendamento.setHoraInicio(horaInicio);
        agendamento.setHoraFim(horaInicio.plusHours(1));

        return agendamentoRepository.save(agendamento);
    }

    /** Verifica se o horário solicitado coincide com um agendamento já existente. */
    private boolean sobrepoe(LocalTime inicio, LocalTime fim, Agendamento agendamento) {
        return inicio.isBefore(agendamento.getHoraFim())
                && fim.isAfter(agendamento.getHoraInicio());
    }
}

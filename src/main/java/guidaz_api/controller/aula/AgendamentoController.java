package guidaz_api.controller.aula;

import guidaz_api.dto.agenda.SlotDisponivelDTO;
import guidaz_api.model.aula.Agendamento;
import guidaz_api.service.aula.AgendamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Expõe os endpoints para consultar horários livres e reservar uma aula. */
@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    /** Executa as consultas e reservas de agendamento. */
    private final AgendamentoService agendamentoService;

    /** Recebe o serviço responsável pelas operações de agendamento. */
    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /** Lista os horários disponíveis de um instrutor em uma data. */
    @GetMapping("/instrutor/{instrutorId}/disponiveis")
    public ResponseEntity<List<SlotDisponivelDTO>> buscarHorariosDisponiveis(
            @PathVariable Long instrutorId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<SlotDisponivelDTO> horarios = agendamentoService.buscarHorariosDisponiveis(instrutorId, data);
        return ResponseEntity.ok(horarios);
    }

    /** Cria uma reserva e retorna o identificador gerado para o agendamento. */
    @PostMapping
    public ResponseEntity<Long> criarAgendamento(@RequestBody CriarAgendamentoRequest request) {
        Agendamento agendamento = agendamentoService.criarAgendamento(
                request.alunoId(),
                request.instrutorId(),
                request.data(),
                request.horaInicio()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento.getId());
    }

    /** Dados necessários para solicitar uma reserva de uma aula de uma hora. */
    public record CriarAgendamentoRequest(
            Long alunoId,
            Long instrutorId,
            LocalDate data,
            LocalTime horaInicio
    ) {}
}

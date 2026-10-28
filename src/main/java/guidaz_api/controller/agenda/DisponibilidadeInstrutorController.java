
package guidaz_api.controller.agenda;

import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import guidaz_api.service.agenda.DisponibilidadeInstrutorService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidades")
@CrossOrigin(origins = "*")
public class DisponibilidadeInstrutorController {

    private final DisponibilidadeInstrutorService service;

    public DisponibilidadeInstrutorController(
            DisponibilidadeInstrutorService service
    ) {
        this.service = service;
    }

    // Consultar todos os períodos ativos de um instrutor.
    @GetMapping("/instrutor/{instrutorId}")
    public List<DisponibilidadeInstrutor> buscarPorInstrutor(
            @PathVariable Long instrutorId
    ) {
        return service.buscarPorInstrutor(instrutorId);
    }

    // Consultar os períodos ativos de um instrutor em um dia da semana.
    @GetMapping("/instrutor/{instrutorId}/dia")
    public List<DisponibilidadeInstrutor> buscarPorInstrutorEDia(
            @PathVariable Long instrutorId,
            @RequestParam DayOfWeek diaSemana
    ) {
        return service.buscarPorInstrutorEDia(
                instrutorId,
                diaSemana
        );
    }

    // Cadastrar um período de disponibilidade.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadeInstrutor criarDisponibilidade(
            @RequestParam Long instrutorId,
            @RequestParam DayOfWeek diaSemana,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaFim
    ) {
        return service.criarDisponibilidade(
                instrutorId,
                diaSemana,
                horaInicio,
                horaFim
        );
    }

    // Consultar os horários livres para aulas de 50 minutos em uma data.
    @GetMapping("/instrutor/{instrutorId}/horarios-livres")
    public List<LocalTime> buscarHorariosLivres(
            @PathVariable Long instrutorId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        return service.buscarHorariosLivres(instrutorId, data);
    }
}


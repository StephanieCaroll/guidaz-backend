
package guidaz_api.controller.agenda;

import guidaz_api.dto.agenda.BloqueioAgendaRequest;
import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.service.agenda.BloqueioAgendaService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bloqueios-agenda")
@CrossOrigin(origins = "*")
public class BloqueioAgendaController {

    private final BloqueioAgendaService service;

    public BloqueioAgendaController(
            BloqueioAgendaService service
    ) {
        this.service = service;
    }

    // Consultar os bloqueios de um instrutor em uma data
    @GetMapping("/instrutor/{instrutorId}")
    public List<BloqueioAgenda> buscarPorInstrutorEData(
            @PathVariable Long instrutorId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        return service.buscarPorInstrutorEData(
                instrutorId,
                data
        );
    }

    // Cadastrar um bloqueio na agenda
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueioAgenda criarBloqueio(
            @RequestBody BloqueioAgendaRequest request
    ) {
        return service.criarBloqueio(
                request.instrutorId(),
                request.data(),
                request.horaInicio(),
                request.horaFim(),
                request.diaInteiro(),
                request.motivo()
        );
    }
}

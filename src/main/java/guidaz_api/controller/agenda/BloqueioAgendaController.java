package guidaz_api.controller.agenda;

import guidaz_api.dto.agenda.BloqueioAgendaDTO;
import guidaz_api.service.agenda.BloqueioAgendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bloqueios")
@CrossOrigin(origins = "*")
public class BloqueioAgendaController {

    private final BloqueioAgendaService bloqueioService;

    public BloqueioAgendaController(BloqueioAgendaService bloqueioService) {
        this.bloqueioService = bloqueioService;
    }

    @GetMapping("/instrutor/{instrutorId}")
    public ResponseEntity<List<BloqueioAgendaDTO>> obterBloqueios(@PathVariable Long instrutorId) {
        List<BloqueioAgendaDTO> bloqueios = bloqueioService.buscarPorInstrutor(instrutorId);
        return ResponseEntity.ok(bloqueios);
    }

    @PostMapping("/instrutor/{instrutorId}")
    public ResponseEntity<BloqueioAgendaDTO> criarBloqueio(
            @PathVariable Long instrutorId,
            @RequestBody BloqueioAgendaDTO dto) {
        BloqueioAgendaDTO novoBloqueio = bloqueioService.criarBloqueio(instrutorId, dto);
        return ResponseEntity.ok(novoBloqueio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerBloqueio(@PathVariable Long id) {
        bloqueioService.removerBloqueio(id);
        return ResponseEntity.noContent().build();
    }
}
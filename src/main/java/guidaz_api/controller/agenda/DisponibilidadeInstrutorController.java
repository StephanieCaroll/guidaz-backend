package guidaz_api.controller.agenda;

import guidaz_api.dto.agenda.DisponibilidadeInstrutorDTO;
import guidaz_api.dto.agenda.SlotDisponivelDTO;
import guidaz_api.service.agenda.DisponibilidadeInstrutorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidades")
@CrossOrigin(origins = "*")
public class DisponibilidadeInstrutorController {

    private final DisponibilidadeInstrutorService disponibilidadeService;

    public DisponibilidadeInstrutorController(DisponibilidadeInstrutorService disponibilidadeService) {
        this.disponibilidadeService = disponibilidadeService;
    }

    @GetMapping("/instrutor/{instrutorId}")
    public ResponseEntity<List<DisponibilidadeInstrutorDTO>> obterDisponibilidades(@PathVariable Long instrutorId) {
        List<DisponibilidadeInstrutorDTO> disponibilidades = disponibilidadeService.buscarPorInstrutor(instrutorId);
        return ResponseEntity.ok(disponibilidades);
    }

    @PostMapping("/instrutor/{instrutorId}")
    public ResponseEntity<List<DisponibilidadeInstrutorDTO>> salvarDisponibilidades(
            @PathVariable Long instrutorId,
            @RequestBody List<DisponibilidadeInstrutorDTO> dtos) {
        List<DisponibilidadeInstrutorDTO> salvas = disponibilidadeService.salvarDisponibilidades(instrutorId, dtos);
        return ResponseEntity.ok(salvas);
    }

    @GetMapping("/instrutor/{instrutorId}/slots")
    public ResponseEntity<List<SlotDisponivelDTO>> obterSlotsDoDia(
            @PathVariable Long instrutorId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<SlotDisponivelDTO> slots = disponibilidadeService.gerarSlotsDoDia(instrutorId, data);
        return ResponseEntity.ok(slots);
    }
}
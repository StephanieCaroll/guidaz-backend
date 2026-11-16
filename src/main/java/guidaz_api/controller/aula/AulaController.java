package guidaz_api.controller.aula;

import guidaz_api.dto.agenda.aula.*;
import guidaz_api.model.aula.Avaliacao;
import guidaz_api.service.agenda.aula.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AulaController {

    private final AulaService aulaService;
    private final HorasService horasService;
    private final ResumoInstrutorService resumoInstrutorService;
    private final AvaliacaoService avaliacaoService;

    @PostMapping({"/aulas", "/agendamentos/instrutor"})
    public ResponseEntity<AulaHistoricoDTO> agendar(@RequestBody AgendarAulaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aulaService.agendar(request));
    }

    @GetMapping("/aulas/{id}")
    public ResponseEntity<AulaHistoricoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(aulaService.buscarPorId(id));
    }

    @GetMapping("/aulas/aluno/{alunoId}")
    public ResponseEntity<List<AulaHistoricoDTO>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(aulaService.listarPorAluno(alunoId));
    }

    @GetMapping("/aulas/aluno/{alunoId}/proximas")
    public ResponseEntity<List<AulaHistoricoDTO>> listarProximasAluno(
            @PathVariable Long alunoId,
            @RequestParam(defaultValue = "3") int limite) {
        return ResponseEntity.ok(aulaService.listarProximasAluno(alunoId, limite));
    }

    @GetMapping("/aulas/instrutor/{instrutorId}")
    public ResponseEntity<List<AulaHistoricoDTO>> listarPorInstrutor(@PathVariable Long instrutorId) {
        return ResponseEntity.ok(aulaService.listarPorInstrutor(instrutorId));
    }

    @GetMapping("/aulas/instrutor/{instrutorId}/proximas")
    public ResponseEntity<List<AulaHistoricoDTO>> listarProximasInstrutor(
            @PathVariable Long instrutorId,
            @RequestParam(defaultValue = "3") int limite) {
        return ResponseEntity.ok(aulaService.listarProximasInstrutor(instrutorId, limite));
    }

    @PatchMapping("/aulas/{id}/concluir")
    public ResponseEntity<AulaHistoricoDTO> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(aulaService.concluir(id));
    }

    @PatchMapping("/aulas/{id}/cancelar")
    public ResponseEntity<AulaHistoricoDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(aulaService.cancelar(id));
    }

    @GetMapping({"/horas/aluno/{alunoId}", "/aulas/aluno/{alunoId}/resumo"})
    public ResponseEntity<ResumoHorasDTO> resumoAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(horasService.calcularResumoAluno(alunoId));
    }

    @GetMapping({"/horas/instrutor/{instrutorId}", "/aulas/instrutor/{instrutorId}/resumo"})
    public ResponseEntity<ResumoInstrutorDTO> resumoInstrutor(@PathVariable Long instrutorId) {
        return ResponseEntity.ok(resumoInstrutorService.gerarResumo(instrutorId));
    }

    @PostMapping({"/aulas/{id}/avaliacoes", "/aulas/{id}/avaliar"})
    public ResponseEntity<Avaliacao> avaliar(@PathVariable Long id, @RequestBody AvaliacaoRequest request) {
        return ResponseEntity.ok(avaliacaoService.avaliar(id, request));
    }
}
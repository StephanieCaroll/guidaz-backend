package guidaz_api.controller.aula;

import guidaz_api.dto.agenda.aula.AgendarAulaRequest;
import guidaz_api.dto.agenda.aula.AulaHistoricoDTO;
import guidaz_api.dto.agenda.aula.AvaliacaoRequest;
import guidaz_api.service.agenda.aula.AulaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aulas")
@CrossOrigin(origins = "*")
public class AulaController {

    @Autowired
    private AulaService aulaService;

    @PostMapping
    public ResponseEntity<AulaHistoricoDTO> agendar(@Valid @RequestBody AgendarAulaRequest request) {
        AulaHistoricoDTO novaAula = aulaService.agendar(request);
        return ResponseEntity.ok(novaAula);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AulaHistoricoDTO> buscarPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ALUNO") String perfil) {
        AulaHistoricoDTO aula = aulaService.buscarPorId(id, perfil);
        return ResponseEntity.ok(aula);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AulaHistoricoDTO> editarAula(
            @PathVariable Long id,
            @RequestBody AgendarAulaRequest request) {
        AulaHistoricoDTO aulaAtualizada = aulaService.editarAula(id, request);
        return ResponseEntity.ok(aulaAtualizada);
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<AulaHistoricoDTO>> historicoAluno(@PathVariable Long alunoId) {
        List<AulaHistoricoDTO> historico = aulaService.historicoAluno(alunoId);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/aluno/{alunoId}/proximas")
    public ResponseEntity<List<AulaHistoricoDTO>> proximasAluno(
            @PathVariable Long alunoId,
            @RequestParam(defaultValue = "3") int limite) {
        List<AulaHistoricoDTO> proximas = aulaService.proximasAluno(alunoId, limite);
        return ResponseEntity.ok(proximas);
    }

    @GetMapping("/instrutor/{instrutorId}")
    public ResponseEntity<List<AulaHistoricoDTO>> historicoInstrutor(@PathVariable Long instrutorId) {
        List<AulaHistoricoDTO> historico = aulaService.historicoInstrutor(instrutorId);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/instrutor/{instrutorId}/proximas")
    public ResponseEntity<List<AulaHistoricoDTO>> proximasInstrutor(
            @PathVariable Long instrutorId,
            @RequestParam(defaultValue = "3") int limite) {
        List<AulaHistoricoDTO> proximas = aulaService.proximasInstrutor(instrutorId, limite);
        return ResponseEntity.ok(proximas);
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<AulaHistoricoDTO> concluirAula(@PathVariable Long id) {
        AulaHistoricoDTO aula = aulaService.concluirAula(id);
        return ResponseEntity.ok(aula);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AulaHistoricoDTO> cancelarAula(@PathVariable Long id) {
        AulaHistoricoDTO aula = aulaService.cancelarAula(id);
        return ResponseEntity.ok(aula);
    }

    @PostMapping("/{id}/avaliacoes")
    public ResponseEntity<Void> avaliar(
            @PathVariable Long id,
            @RequestBody AvaliacaoRequest request) {
        aulaService.avaliar(id, request);
        return ResponseEntity.ok().build();
    }
}
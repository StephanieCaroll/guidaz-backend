package guidaz_api.controller.notificacao;

import guidaz_api.model.notificacao.Notificacao;
import guidaz_api.service.notificacao.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listar(
            @RequestParam Long usuarioId,
            @RequestParam String perfil
    ) {
        return ResponseEntity.ok(service.listar(usuarioId, perfil));
    }

    @PutMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        service.marcarComoLida(id);
        return ResponseEntity.noContent().build();
    }
}
package guidaz_api.controller.pedido;

import guidaz_api.dto.pedido.PacoteDTO;
import guidaz_api.service.pedido.PedidoPacoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pacotes")
@CrossOrigin(origins = "*")
public class PacoteController {
    private final PedidoPacoteService service;
    public PacoteController(PedidoPacoteService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<PacoteDTO>> listar() { return ResponseEntity.ok(service.listarPacotes()); }
}

package guidaz_api.controller.pedido;

import guidaz_api.dto.pedido.*;
import guidaz_api.model.pedido.StatusPedido;
import guidaz_api.service.pedido.PedidoPacoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos-pacote")
@CrossOrigin(origins = "*")
public class PedidoPacoteController {
    private final PedidoPacoteService service;
    public PedidoPacoteController(PedidoPacoteService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<PedidoPacoteDTO> criar(@RequestBody CriarPedidoRequest request) {
        PedidoPacoteDTO pedido = service.criar(request);
        return ResponseEntity.created(URI.create("/api/pedidos-pacote/" + pedido.id())).body(pedido);
    }
    @GetMapping("/{id}") public ResponseEntity<PedidoPacoteDTO> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscar(id)); }
    @GetMapping("/aluno/{alunoId}") public ResponseEntity<List<PedidoPacoteDTO>> aluno(@PathVariable Long alunoId) { return ResponseEntity.ok(service.listarAluno(alunoId)); }
    @GetMapping("/instrutor/{instrutorId}")
    public ResponseEntity<List<PedidoPacoteDTO>> instrutor(@PathVariable Long instrutorId,
            @RequestParam(required = false) StatusPedido status) {
        return ResponseEntity.ok(service.listarInstrutor(instrutorId, status));
    }
    @PostMapping("/{id}/comprovante-informado")
    public ResponseEntity<PedidoPacoteDTO> comprovante(@PathVariable Long id, @RequestBody InformarComprovanteRequest request) {
        return ResponseEntity.ok(service.informarComprovante(id, request));
    }
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoPacoteDTO> cancelar(@PathVariable Long id, @RequestBody CancelarPedidoRequest request) {
        return ResponseEntity.ok(service.cancelar(id, request));
    }
    @PostMapping("/{id}/aprovar")
    public ResponseEntity<PedidoPacoteDTO> aprovar(@PathVariable Long id, @RequestBody AprovarPedidoRequest request) {
        return ResponseEntity.ok(service.aprovar(id, request));
    }
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<PedidoPacoteDTO> rejeitar(@PathVariable Long id, @RequestBody RejeitarPedidoRequest request) {
        return ResponseEntity.ok(service.rejeitar(id, request));
    }
}

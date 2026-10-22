package guidaz_api.controller.usuario;

import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/instrutores")
@CrossOrigin(origins = "*")
public class InstrutorController {

    @Autowired
    private InstrutorRepository instrutorRepository;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Instrutor instrutor) {
        try {
            instrutor.setPerfil("instrutor");
            instrutorRepository.save(instrutor);
            return ResponseEntity.ok("Conta de instrutor cadastrada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar instrutor: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Instrutor>> listarTodos() {
        List<Instrutor> instrutores = instrutorRepository.findAll();
        return ResponseEntity.ok(instrutores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Instrutor> instrutor = instrutorRepository.findById(id);
        if (instrutor.isPresent()) {
            return ResponseEntity.ok(instrutor.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Instrutor instrutorAtualizado) {
        Optional<Instrutor> instrutorOpt = instrutorRepository.findById(id);
        if (instrutorOpt.isPresent()) {
            Instrutor instrutor = instrutorOpt.get();
            instrutor.setNome(instrutorAtualizado.getNome());
            instrutor.setEmail(instrutorAtualizado.getEmail());
            instrutor.setTelefone(instrutorAtualizado.getTelefone());
            instrutor.setCep(instrutorAtualizado.getCep());
            instrutor.setLogradouro(instrutorAtualizado.getLogradouro());
            instrutor.setNumero(instrutorAtualizado.getNumero());
            instrutor.setBairro(instrutorAtualizado.getBairro());
            instrutor.setCidade(instrutorAtualizado.getCidade());
            instrutor.setCategoriasLecionadas(instrutorAtualizado.getCategoriasLecionadas());
            instrutor.setBiografia(instrutorAtualizado.getBiografia());
            instrutor.setVeiculoModelo(instrutorAtualizado.getVeiculoModelo());
            instrutor.setVeiculoAno(instrutorAtualizado.getVeiculoAno());
            instrutor.setVeiculoPlaca(instrutorAtualizado.getVeiculoPlaca());

            if (instrutorAtualizado.getSenha() != null && !instrutorAtualizado.getSenha().isEmpty()) {
                instrutor.setSenha(instrutorAtualizado.getSenha());
            }

            instrutorRepository.save(instrutor);
            return ResponseEntity.ok("Conta de instrutor editada com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (instrutorRepository.existsById(id)) {
            instrutorRepository.deleteById(id);
            return ResponseEntity.ok().body("Conta de instrutor excluída com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }
}
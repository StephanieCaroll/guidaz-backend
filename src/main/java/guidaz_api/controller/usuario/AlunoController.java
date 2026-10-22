package guidaz_api.controller.usuario;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.repository.usuario.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Aluno aluno) {
        try {
            aluno.setPerfil("aluno");
            alunoRepository.save(aluno);
            return ResponseEntity.ok("Conta de aluno cadastrada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar aluno: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Aluno>> listarTodos() {
        List<Aluno> alunos = alunoRepository.findAll();
        return ResponseEntity.ok(alunos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Aluno> aluno = alunoRepository.findById(id);
        if (aluno.isPresent()) {
            return ResponseEntity.ok(aluno.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Aluno alunoAtualizado) {
        Optional<Aluno> alunoOpt = alunoRepository.findById(id);
        if (alunoOpt.isPresent()) {
            Aluno aluno = alunoOpt.get();
            aluno.setNome(alunoAtualizado.getNome());
            aluno.setEmail(alunoAtualizado.getEmail());
            aluno.setTelefone(alunoAtualizado.getTelefone());
            aluno.setCep(alunoAtualizado.getCep());
            aluno.setLogradouro(alunoAtualizado.getLogradouro());
            aluno.setNumero(alunoAtualizado.getNumero());
            aluno.setBairro(alunoAtualizado.getBairro());
            aluno.setCidade(alunoAtualizado.getCidade());
            aluno.setCategoriaPretendida(alunoAtualizado.getCategoriaPretendida());
            
            if (alunoAtualizado.getSenha() != null && !alunoAtualizado.getSenha().isEmpty()) {
                aluno.setSenha(alunoAtualizado.getSenha());
            }

            alunoRepository.save(aluno);
            return ResponseEntity.ok("Conta de aluno Editada com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (alunoRepository.existsById(id)) {
            alunoRepository.deleteById(id);
            return ResponseEntity.ok().body("Conta de aluno excluída com sucesso.");
        }
        return ResponseEntity.notFound().build();
    }
}
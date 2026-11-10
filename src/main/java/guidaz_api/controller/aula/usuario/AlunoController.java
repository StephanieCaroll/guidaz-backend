package guidaz_api.controller.usuario;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.repository.usuario.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> uploadFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) {
        Optional<Aluno> alunoOpt = alunoRepository.findById(id);
        if (alunoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (foto == null || foto.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo de foto não fornecido.");
        }

        try {
            Aluno aluno = alunoOpt.get();
            aluno.setFotoData(foto.getBytes());
            aluno.setFotoType(foto.getContentType());
            aluno.setFotoUrl("/api/alunos/" + id + "/foto");

            alunoRepository.save(aluno);

            return ResponseEntity.ok(aluno);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao salvar foto de perfil: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> buscarFoto(@PathVariable Long id) {
        Optional<Aluno> alunoOpt = alunoRepository.findById(id);

        if (alunoOpt.isEmpty() || alunoOpt.get().getFotoData() == null) {
            return ResponseEntity.notFound().build();
        }

        Aluno aluno = alunoOpt.get();
        MediaType mediaType = MediaType.IMAGE_JPEG;

        if (aluno.getFotoType() != null && !aluno.getFotoType().isEmpty()) {
            try {
                mediaType = MediaType.parseMediaType(aluno.getFotoType());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(aluno.getFotoData());
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
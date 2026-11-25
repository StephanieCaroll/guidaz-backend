package guidaz_api.controller.usuario;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.repository.usuario.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Aluno aluno) {
        try {
            aluno.setPerfil("aluno");
            if (aluno.getSenha() != null && !aluno.getSenha().isBlank()) {
                aluno.setSenha(codificarSenhaSeNecessario(aluno.getSenha()));
            }
            alunoRepository.save(aluno);
            return ResponseEntity.ok("Conta de aluno cadastrada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar aluno: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        List<Aluno> alunos = alunoRepository.findAll();
        List<Map<String, Object>> respostaSegura = new ArrayList<>();
        for (Aluno aluno : alunos) {
            respostaSegura.add(criarMapaAlunoSeguro(aluno));
        }
        return ResponseEntity.ok(respostaSegura);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Aluno> aluno = alunoRepository.findById(id);
        if (aluno.isPresent()) {
            return ResponseEntity.ok(criarMapaAlunoSeguro(aluno.get()));
        }
        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> criarMapaAlunoSeguro(Aluno aluno) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", aluno.getId());
        item.put("nome", aluno.getNome());
        item.put("email", aluno.getEmail());
        item.put("telefone", aluno.getTelefone());
        item.put("perfil", aluno.getPerfil() != null ? aluno.getPerfil() : "aluno");
        item.put("fotoUrl", aluno.getFotoUrl());
        item.put("fotoType", aluno.getFotoType());
        item.put("bairro", aluno.getBairro());
        item.put("cidade", aluno.getCidade());
        item.put("categoriaPretendida", aluno.getCategoriaPretendida());
        return item;
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
                aluno.setSenha(codificarSenhaSeNecessario(alunoAtualizado.getSenha()));
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

            return ResponseEntity.ok(criarMapaAlunoSeguro(aluno));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao salvar foto de perfil: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<?> buscarFoto(@PathVariable Long id) {
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

    private String codificarSenhaSeNecessario(String senha) {
        if (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$")) {
            return senha;
        }
        return passwordEncoder.encode(senha);
    }
}
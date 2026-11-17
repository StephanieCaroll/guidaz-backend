package guidaz_api.controller.usuario;

import guidaz_api.model.aula.Aula;
import guidaz_api.model.pedido.PedidoPacote;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.pedido.PedidoPacoteRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/instrutores")
@CrossOrigin(origins = "*")
public class InstrutorController {

    @Autowired
    private InstrutorRepository instrutorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private PedidoPacoteRepository pedidoPacoteRepository;

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

    @GetMapping("/{id}/alunos")
    public ResponseEntity<?> listarAlunosDoInstrutor(@PathVariable Long id) {
        try {
            if (!instrutorRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Queries com JOIN FETCH: já trazem o aluno junto (sem lazy loading)
            List<Aula> aulasInstrutor = aulaRepository.buscarPorInstrutorComAluno(id);
            List<PedidoPacote> pedidosInstrutor = pedidoPacoteRepository.buscarPorInstrutorComAluno(id);

            Map<Long, Map<String, Object>> alunosMap = new LinkedHashMap<>();

            for (PedidoPacote pedido : pedidosInstrutor) {
                Aluno aluno = pedido.getAluno();
                if (aluno != null && aluno.getId() != null) {
                    alunosMap.putIfAbsent(aluno.getId(), criarMapaAlunoSeguro(aluno, aluno.getId()));
                }
            }

            for (Aula aula : aulasInstrutor) {
                Aluno aluno = aula.getAluno();
                if (aluno != null && aluno.getId() != null) {
                    alunosMap.putIfAbsent(aluno.getId(), criarMapaAlunoSeguro(aluno, aluno.getId()));
                }
            }

            List<Map<String, Object>> resultado = new ArrayList<>(alunosMap.values());
            return ResponseEntity.ok(resultado);

        } catch (Throwable e) {
            // Throwable (e não só Exception) para também capturar Errors e mostrar a causa real
            e.printStackTrace();

            Throwable causa = e;
            while (causa.getCause() != null && causa.getCause() != causa) {
                causa = causa.getCause();
            }

            Map<String, Object> erro = new LinkedHashMap<>();
            erro.put("erro", e.getClass().getName() + ": " + e.getMessage());
            erro.put("causaRaiz", causa.getClass().getName() + ": " + causa.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }

    private Map<String, Object> criarMapaAlunoSeguro(Aluno aluno, Long alunoId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", alunoId);
        item.put("nome", aluno.getNome() != null ? aluno.getNome() : "Aluno #" + alunoId);
        item.put("email", aluno.getEmail() != null ? aluno.getEmail() : "");
        item.put("telefone", aluno.getTelefone() != null ? aluno.getTelefone() : "");
        item.put("fotoUrl", aluno.getFotoUrl() != null ? aluno.getFotoUrl() : "");
        item.put("categoria", aluno.getCategoriaPretendida() != null ? aluno.getCategoriaPretendida() : "B");
        item.put("status", "Ativo");
        item.put("aulasConcluidas", 0);
        item.put("aulasAgendadas", 0);
        item.put("aulasContratadas", 10);
        item.put("aulasRestantes", 10);
        item.put("progressoPercentual", 0);
        item.put("proximaAula", null);
        return item;
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

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> uploadFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) {
        Optional<Instrutor> instrutorOpt = instrutorRepository.findById(id);
        if (instrutorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (foto == null || foto.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo de foto não fornecido.");
        }

        try {
            Instrutor instrutor = instrutorOpt.get();
            instrutor.setFotoData(foto.getBytes());
            instrutor.setFotoType(foto.getContentType());
            instrutor.setFotoUrl("/api/instrutores/" + id + "/foto");

            instrutorRepository.save(instrutor);

            return ResponseEntity.ok(instrutor);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao salvar foto de perfil: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> buscarFoto(@PathVariable Long id) {
        Optional<Instrutor> instrutorOpt = instrutorRepository.findById(id);

        if (instrutorOpt.isEmpty() || instrutorOpt.get().getFotoData() == null) {
            return ResponseEntity.notFound().build();
        }

        Instrutor instrutor = instrutorOpt.get();
        MediaType mediaType = MediaType.IMAGE_JPEG;

        if (instrutor.getFotoType() != null && !instrutor.getFotoType().isEmpty()) {
            try {
                mediaType = MediaType.parseMediaType(instrutor.getFotoType());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(instrutor.getFotoData());
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
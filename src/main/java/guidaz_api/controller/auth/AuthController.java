package guidaz_api.controller.auth;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.model.usuario.Usuario;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Login de aluno e instrutor: POST /api/auth/login com { "email": ..., "senha": ... }.
 *
 * O cadastro grava a senha com BCrypt, então a conferência precisa ser feita aqui no
 * back-end (o front não recebe mais a senha). Contas antigas, criadas antes da
 * criptografia, ainda têm a senha em texto: elas entram normalmente e, nesse primeiro
 * login, a senha é convertida para BCrypt.
 *
 * Projeto acadêmico: não há token/JWT; o front guarda os dados do usuário na sessão local.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final String MENSAGEM_INVALIDO = "E-mail ou senha incorretos.";

    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AlunoRepository alunoRepository,
                          InstrutorRepository instrutorRepository,
                          PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record LoginRequest(String email, String senha) {}

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequest request) {
        if (request == null || vazio(request.email()) || vazio(request.senha())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe e-mail e senha."));
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String senha = request.senha();

        Optional<Aluno> aluno = alunoRepository.findByEmail(email);
        if (aluno.isPresent() && senhaConfere(aluno.get(), senha)) {
            alunoRepository.save(aluno.get());
            return ResponseEntity.ok(aluno.get());
        }

        Optional<Instrutor> instrutor = instrutorRepository.findByEmail(email);
        if (instrutor.isPresent() && senhaConfere(instrutor.get(), senha)) {
            instrutorRepository.save(instrutor.get());
            return ResponseEntity.ok(instrutor.get());
        }

        // Mesma resposta para e-mail inexistente e senha errada, para não revelar quais e-mails existem.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", MENSAGEM_INVALIDO));
    }

    /**
     * Confere a senha digitada. Se a conta ainda guarda a senha em texto (cadastro antigo)
     * e ela confere, já troca pelo hash BCrypt.
     */
    private boolean senhaConfere(Usuario usuario, String senhaDigitada) {
        String guardada = usuario.getSenha();
        if (vazio(guardada)) {
            return false;
        }
        if (ehBcrypt(guardada)) {
            return passwordEncoder.matches(senhaDigitada, guardada);
        }
        if (guardada.equals(senhaDigitada)) {
            usuario.setSenha(passwordEncoder.encode(senhaDigitada));
            return true;
        }
        return false;
    }

    private boolean ehBcrypt(String senha) {
        return senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$");
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}

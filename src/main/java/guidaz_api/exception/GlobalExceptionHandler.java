package guidaz_api.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<?> conflito(RegraNegocioException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DadoInvalidoException.class)
    public ResponseEntity<?> invalido(DadoInvalidoException e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflitoBanco(DataIntegrityViolationException e) {
        String detalhe = extrairMensagemCompleta(e).toLowerCase();

        if (detalhe.contains("cpf")) {
            return resposta(HttpStatus.CONFLICT, "Cpf ja cadastrado");
        }
        if (detalhe.contains("email")) {
            return resposta(HttpStatus.CONFLICT, "E-mail ja cadastrado");
        }

        return resposta(
                HttpStatus.CONFLICT,
                "Um dos horários acabou de ser reservado. Atualize a disponibilidade e tente novamente."
        );
    }

    private String extrairMensagemCompleta(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable atual = throwable;
        while (atual != null) {
            if (atual.getMessage() != null) {
                sb.append(atual.getMessage()).append(" ");
            }
            atual = atual.getCause();
        }
        return sb.toString();
    }

    private ResponseEntity<?> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
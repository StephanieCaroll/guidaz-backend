package guidaz_api.controller.usuario;

import guidaz_api.dto.usuario.PreferenciasDTO;
import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.usuario.AlunoRepository;
import guidaz_api.repository.usuario.InstrutorRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PreferenciasController {

    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;

    public PreferenciasController(
            AlunoRepository alunoRepository,
            InstrutorRepository instrutorRepository
    ) {
        this.alunoRepository = alunoRepository;
        this.instrutorRepository = instrutorRepository;
    }

    // BUSCAR PREFERÊNCIAS DO ALUNO
    @GetMapping("/alunos/{id}/preferencias")
    public ResponseEntity<PreferenciasDTO> buscarPreferenciasAluno(
            @PathVariable Long id
    ) {
        return alunoRepository.findById(id)
                .map(aluno -> {
                    aplicarPadroes(aluno);
                    alunoRepository.save(aluno);
                    return ResponseEntity.ok(converter(aluno));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ATUALIZAR PREFERÊNCIAS DO ALUNO
    @PutMapping("/alunos/{id}/preferencias")
    public ResponseEntity<PreferenciasDTO> atualizarPreferenciasAluno(
            @PathVariable Long id,
            @RequestBody PreferenciasDTO dto
    ) {
        return alunoRepository.findById(id)
                .map(aluno -> {
                    atualizar(aluno, dto);
                    alunoRepository.save(aluno);
                    return ResponseEntity.ok(converter(aluno));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // BUSCAR PREFERÊNCIAS DO INSTRUTOR
    @GetMapping("/instrutores/{id}/preferencias")
    public ResponseEntity<PreferenciasDTO> buscarPreferenciasInstrutor(
            @PathVariable Long id
    ) {
        return instrutorRepository.findById(id)
                .map(instrutor -> {
                    aplicarPadroes(instrutor);
                    instrutorRepository.save(instrutor);
                    return ResponseEntity.ok(converter(instrutor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ATUALIZAR PREFERÊNCIAS DO INSTRUTOR
    @PutMapping("/instrutores/{id}/preferencias")
    public ResponseEntity<PreferenciasDTO> atualizarPreferenciasInstrutor(
            @PathVariable Long id,
            @RequestBody PreferenciasDTO dto
    ) {
        return instrutorRepository.findById(id)
                .map(instrutor -> {
                    atualizar(instrutor, dto);
                    instrutorRepository.save(instrutor);
                    return ResponseEntity.ok(converter(instrutor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // APLICAR VALORES PADRÃO
    private void aplicarPadroes(guidaz_api.model.usuario.Usuario usuario) {
        if (usuario.getTemaEscuro() == null) {
            usuario.setTemaEscuro(false);
        }

        if (usuario.getTamanhoTexto() == null) {
            usuario.setTamanhoTexto("Normal");
        }

        if (usuario.getIdioma() == null) {
            usuario.setIdioma("Portugu\u00eas (BR)");
        }

        if (usuario.getNotificacoes() == null) {
            usuario.setNotificacoes(true);
        }
    }

    // ATUALIZAR SOMENTE OS CAMPOS ENVIADOS
    private void atualizar(
            guidaz_api.model.usuario.Usuario usuario,
            PreferenciasDTO dto
    ) {
        if (dto.temaEscuro() != null) {
            usuario.setTemaEscuro(dto.temaEscuro());
        }

        if (dto.tamanhoTexto() != null) {
            if (!dto.tamanhoTexto().matches("Pequeno|Normal|Grande")) {
                throw new IllegalArgumentException("Tamanho de texto inválido.");
            }
            usuario.setTamanhoTexto(dto.tamanhoTexto());
        }

        if (dto.idioma() != null) {
            if (!dto.idioma().matches("Portugu\\u00eas \\(BR\\)|English \\(US\\)|Espa\\u00f1ol")) {
                throw new IllegalArgumentException("Idioma inválido.");
            }
            usuario.setIdioma(dto.idioma());
        }

        if (dto.notificacoes() != null) {
            usuario.setNotificacoes(dto.notificacoes());
        }
    }

    // CONVERTER PARA DTO
    private PreferenciasDTO converter(
            guidaz_api.model.usuario.Usuario usuario
    ) {
        return new PreferenciasDTO(
                usuario.getTemaEscuro(),
                usuario.getTamanhoTexto(),
                usuario.getIdioma(),
                usuario.getNotificacoes()
        );
    }
}
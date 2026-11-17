package guidaz_api.service.notificacao;

import guidaz_api.model.notificacao.Notificacao;
import guidaz_api.repository.notificacao.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void criar(Long destinatarioId, String perfilDestinatario, String tipo, String titulo, String descricao) {
        criar(destinatarioId, perfilDestinatario, tipo, titulo, descricao, null, null);
    }

    @Transactional
    public void criar(
            Long destinatarioId,
            String perfilDestinatario,
            String tipo,
            String titulo,
            String descricao,
            String referenciaTipo,
            Long referenciaId
    ) {
        if (destinatarioId == null || perfilDestinatario == null) return;
        Notificacao n = new Notificacao(
                destinatarioId,
                perfilDestinatario.toUpperCase(),
                tipo,
                titulo,
                descricao,
                referenciaTipo,
                referenciaId
        );
        repository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar(Long destinatarioId, String perfil) {
        return repository.findByDestinatarioIdAndPerfilDestinatarioIgnoreCaseOrderByCriadaEmDesc(
                destinatarioId,
                perfil
        );
    }

    @Transactional
    public void marcarComoLida(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setLida(true);
            repository.save(n);
        });
    }
}
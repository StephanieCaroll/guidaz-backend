package guidaz_api.repository.notificacao;

import guidaz_api.model.notificacao.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByDestinatarioIdAndPerfilDestinatarioIgnoreCaseOrderByCriadaEmDesc(
            Long destinatarioId,
            String perfilDestinatario
    );
}
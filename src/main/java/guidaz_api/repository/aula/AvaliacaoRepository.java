package guidaz_api.repository.aula;

import guidaz_api.model.aula.Avaliacao;
import guidaz_api.model.aula.TipoAvaliador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByAulaId(Long aulaId);
    Optional<Avaliacao> findByAulaIdAndTipoAvaliador(Long aulaId, TipoAvaliador tipoAvaliador);
}
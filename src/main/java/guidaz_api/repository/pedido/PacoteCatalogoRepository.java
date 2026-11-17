package guidaz_api.repository.pedido;

import guidaz_api.model.pedido.PacoteCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacoteCatalogoRepository extends JpaRepository<PacoteCatalogo, Long> {
    List<PacoteCatalogo> findByInstrutorIdIsNullAndAtivoTrueOrderByIdAsc();
    List<PacoteCatalogo> findByInstrutorIdAndAtivoTrueOrderByIdAsc(Long instrutorId);
    Optional<PacoteCatalogo> findByInstrutorIdAndCategoriaAndAtivoTrue(Long instrutorId, String categoria);
    Optional<PacoteCatalogo> findByInstrutorIdIsNullAndCategoriaAndAtivoTrue(String categoria);
    long countByInstrutorIdIsNull();
}
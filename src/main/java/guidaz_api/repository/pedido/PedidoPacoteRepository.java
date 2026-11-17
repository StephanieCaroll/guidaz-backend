package guidaz_api.repository.pedido;

import guidaz_api.model.pedido.PedidoPacote;
import guidaz_api.model.pedido.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PedidoPacoteRepository extends JpaRepository<PedidoPacote, Long> {
    List<PedidoPacote> findByAlunoIdOrderByCriadoEmDesc(Long alunoId);
    List<PedidoPacote> findByInstrutorIdAndStatusOrderByCriadoEmDesc(Long instrutorId, StatusPedido status);
    List<PedidoPacote> findByInstrutorIdOrderByCriadoEmDesc(Long instrutorId);

    @Query("""
        SELECT p FROM PedidoPacote p
        JOIN FETCH p.aluno
        WHERE p.instrutor.id = :instrutorId
        ORDER BY p.criadoEm DESC
    """)
    List<PedidoPacote> buscarPorInstrutorComAluno(@Param("instrutorId") Long instrutorId);
}
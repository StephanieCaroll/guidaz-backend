package guidaz_api.repository.pedido;

import guidaz_api.model.pedido.PedidoPacoteHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PedidoPacoteHorarioRepository extends JpaRepository<PedidoPacoteHorario, Long> {
    boolean existsByInstrutorIdAndDataAndHoraInicioAndReservaAtiva(Long instrutorId, LocalDate data, LocalTime horaInicio, Integer reservaAtiva);
    boolean existsByAlunoIdAndDataAndHoraInicioAndReservaAtiva(Long alunoId, LocalDate data, LocalTime horaInicio, Integer reservaAtiva);
    List<PedidoPacoteHorario> findByInstrutorIdAndDataAndReservaAtiva(Long instrutorId, LocalDate data, Integer reservaAtiva);
    List<PedidoPacoteHorario> findByPedidoIdOrderByDataAscHoraInicioAsc(Long pedidoId);
}

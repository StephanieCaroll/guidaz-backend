
package guidaz_api.repository.agenda;

import guidaz_api.model.agenda.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloqueioAgendaRepository
        extends JpaRepository<BloqueioAgenda, Long> {

    List<BloqueioAgenda> findByInstrutorIdAndData(
            Long instrutorId,
            LocalDate data
    );
}

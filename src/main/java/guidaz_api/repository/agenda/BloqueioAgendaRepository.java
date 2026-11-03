package guidaz_api.repository.agenda;

import guidaz_api.model.agenda.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {
    List<BloqueioAgenda> findByInstrutorId(Long instrutorId);
    List<BloqueioAgenda> findByInstrutorIdAndData(Long instrutorId, LocalDate data);
}
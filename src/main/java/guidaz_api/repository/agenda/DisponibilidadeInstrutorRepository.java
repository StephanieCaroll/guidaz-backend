package guidaz_api.repository.agenda;

import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DisponibilidadeInstrutorRepository extends JpaRepository<DisponibilidadeInstrutor, Long> {
    List<DisponibilidadeInstrutor> findByInstrutorId(Long instrutorId);
    List<DisponibilidadeInstrutor> findByInstrutorIdAndDiaSemanaAndAtivoTrue(Long instrutorId, DayOfWeek diaSemana);
    void deleteByInstrutorId(Long instrutorId);
}

package guidaz_api.repository.agenda;

import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DisponibilidadeInstrutorRepository
        extends JpaRepository<DisponibilidadeInstrutor, Long> {

    List<DisponibilidadeInstrutor> findByInstrutorIdAndDiaSemanaAndAtivoTrue(
            Long instrutorId,
            DayOfWeek diaSemana
    );

    List<DisponibilidadeInstrutor> findByInstrutorIdAndAtivoTrue(
            Long instrutorId
    );
}


package guidaz_api.repository.aula;

import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.StatusAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {

    List<Aula> findByAlunoIdOrderByDataAulaAscHoraInicioAsc(Long alunoId);

    List<Aula> findByInstrutorIdOrderByDataAulaAscHoraInicioAsc(Long instrutorId);

    List<Aula> findByAlunoIdAndStatusOrderByDataAulaAscHoraInicioAsc(Long alunoId, StatusAula status);

    List<Aula> findByInstrutorIdAndStatusOrderByDataAulaAscHoraInicioAsc(Long instrutorId, StatusAula status);

    @Query("""
        SELECT COUNT(a) > 0 FROM Aula a
        WHERE a.instrutor.id = :instrutorId
          AND a.dataAula = :dataAula
          AND a.status = 'AGENDADA'
          AND (a.horaInicio < :horaFim AND a.horaFim > :horaInicio)
    """)
    boolean existeConflitoHorarioInstrutor(
            @Param("instrutorId") Long instrutorId,
            @Param("dataAula") LocalDate dataAula,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );

    @Query("""
        SELECT COUNT(a) > 0 FROM Aula a
        WHERE a.aluno.id = :alunoId
          AND a.dataAula = :dataAula
          AND a.status = 'AGENDADA'
          AND (a.horaInicio < :horaFim AND a.horaFim > :horaInicio)
    """)
    boolean existeConflitoHorarioAluno(
            @Param("alunoId") Long alunoId,
            @Param("dataAula") LocalDate dataAula,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}
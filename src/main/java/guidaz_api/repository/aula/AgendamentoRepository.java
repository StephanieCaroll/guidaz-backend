package guidaz_api.repository.aula;

import guidaz_api.model.aula.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/** Fornece operações de persistência e busca para os agendamentos de aulas. */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    /** Busca as reservas de um instrutor em uma data específica. */
    List<Agendamento> findByInstrutorIdAndData(Long instrutorId, LocalDate data);

    /** Lista as reservas realizadas por um aluno. */
    List<Agendamento> findByAlunoId(Long alunoId);
}

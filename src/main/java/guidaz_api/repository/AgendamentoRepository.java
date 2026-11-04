package guidaz_api.repository;

import guidaz_api.model.agendamento.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    // Método útil para listar as aulas marcadas de um instrutor específico
    List<Agendamento> findByInstrutorId(Long instrutorId);

    // Método útil para listar as aulas marcadas de um aluno específico
    List<Agendamento> findByAlunoId(Long alunoId);
}
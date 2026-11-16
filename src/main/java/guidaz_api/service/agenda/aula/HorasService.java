package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.ResumoHorasDTO;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.usuario.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HorasService {

    private final AulaRepository aulaRepository;
    private final AlunoRepository alunoRepository;

    public ResumoHorasDTO calcularResumoAluno(Long alunoId) {
        List<Aula> aulas = aulaRepository.findByAlunoIdOrderByDataAulaAscHoraInicioAsc(alunoId);

        double realizadas = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.CONCLUIDA)
                .mapToDouble(a -> Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes() / 60.0)
                .sum();

        double pendentes = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.AGENDADA)
                .mapToDouble(a -> Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes() / 60.0)
                .sum();

        double contratadas = realizadas + pendentes;
        int percentual = contratadas > 0 ? (int) Math.round((realizadas / contratadas) * 100.0) : 0;

        String categoria = alunoRepository.findById(alunoId)
                .map(a -> "Categoria " + a.getCategoriaPretendida())
                .orElse("Categoria B");

        return ResumoHorasDTO.builder()
                .horasContratadas(Math.round(contratadas * 10.0) / 10.0)
                .horasRealizadas(Math.round(realizadas * 10.0) / 10.0)
                .horasPendentes(Math.round(pendentes * 10.0) / 10.0)
                .percentualConcluido(percentual)
                .categoriaPrincipal(categoria)
                .build();
    }
}
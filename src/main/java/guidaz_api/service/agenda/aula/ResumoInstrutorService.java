package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.ResumoInstrutorDTO;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.Avaliacao;
import guidaz_api.model.aula.StatusAula;
import guidaz_api.model.aula.TipoAvaliador;
import guidaz_api.model.pedido.StatusPedido;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.aula.AvaliacaoRepository;
import guidaz_api.repository.pedido.PedidoPacoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumoInstrutorService {

    private final AulaRepository aulaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PedidoPacoteRepository pedidoPacoteRepository;

    public ResumoInstrutorDTO gerarResumo(Long instrutorId) {
        List<Aula> aulas = aulaRepository.findByInstrutorIdOrderByDataAulaAscHoraInicioAsc(instrutorId);

        long total = aulas.size();
        long realizadas = aulas.stream().filter(a -> a.getStatus() == StatusAula.CONCLUIDA).count();
        long canceladas = aulas.stream().filter(a -> a.getStatus() == StatusAula.CANCELADA).count();
        long faltas = aulas.stream().filter(a -> a.getStatus() == StatusAula.FALTA_ALUNO).count();
        long agendadas = aulas.stream().filter(a -> a.getStatus() == StatusAula.AGENDADA).count();

        double horasRealizadas = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.CONCLUIDA)
                .mapToDouble(a -> Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes() / 60.0)
                .sum();

        double horasPendentes = aulas.stream()
                .filter(a -> a.getStatus() == StatusAula.AGENDADA)
                .mapToDouble(a -> Duration.between(a.getHoraInicio(), a.getHoraFim()).toMinutes() / 60.0)
                .sum();

        List<Integer> notas = aulas.stream()
                .flatMap(a -> avaliacaoRepository.findByAulaIdAndTipoAvaliador(a.getId(), TipoAvaliador.ALUNO).stream())
                .map(Avaliacao::getNota)
                .toList();

        Double media = notas.isEmpty()
                ? null
                : Math.round(notas.stream().mapToInt(Integer::intValue).average().orElse(0.0) * 10.0) / 10.0;

        long totalAlunos = aulas.stream().map(a -> a.getAluno().getId()).distinct().count();
        long pedidosPendentes = pedidoPacoteRepository
                .findByInstrutorIdAndStatusOrderByCriadoEmDesc(instrutorId, StatusPedido.EM_ANALISE)
                .size();

        return ResumoInstrutorDTO.builder()
                .totalAulas(total)
                .realizadas(realizadas)
                .canceladas(canceladas)
                .faltas(faltas)
                .agendadas(agendadas)
                .horasRealizadas(Math.round(horasRealizadas * 10.0) / 10.0)
                .horasPendentes(Math.round(horasPendentes * 10.0) / 10.0)
                .mediaAvaliacoes(media)
                .totalAlunos(totalAlunos)
                .pedidosPendentes(pedidosPendentes)
                .build();
    }
}
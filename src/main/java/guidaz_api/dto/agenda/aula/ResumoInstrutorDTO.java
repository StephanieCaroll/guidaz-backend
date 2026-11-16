package guidaz_api.dto.agenda.aula;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoInstrutorDTO {
    private long totalAulas;
    private long realizadas;
    private long canceladas;
    private long faltas;
    private long agendadas;
    private double horasRealizadas;
    private double horasPendentes;
    private Double mediaAvaliacoes;
    private long totalAlunos;
    private long pedidosPendentes;
}
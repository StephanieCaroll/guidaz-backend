package guidaz_api.dto.agenda.aula;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoHorasDTO {
    private double horasContratadas;
    private double horasRealizadas;
    private double horasPendentes;
    private int percentualConcluido;
    private String categoriaPrincipal;
}
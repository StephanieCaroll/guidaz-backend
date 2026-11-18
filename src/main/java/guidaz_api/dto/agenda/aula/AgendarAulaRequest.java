package guidaz_api.dto.agenda.aula;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendarAulaRequest {
    private Long alunoId;
    private Long instrutorId;
    private Long pedidoPacoteId;
    private LocalDate dataAula;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String categoria;
    private String localEncontro;
    private String observacoes;
}
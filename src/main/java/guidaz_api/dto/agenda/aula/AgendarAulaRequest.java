package guidaz_api.dto.agenda.aula;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
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
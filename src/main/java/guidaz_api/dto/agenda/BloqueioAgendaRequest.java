
package guidaz_api.dto.agenda;

import java.time.LocalDate;
import java.time.LocalTime;

public record BloqueioAgendaRequest(
        Long instrutorId,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean diaInteiro,
        String motivo
) {
}


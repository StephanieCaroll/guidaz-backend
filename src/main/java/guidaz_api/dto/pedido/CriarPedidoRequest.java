package guidaz_api.dto.pedido;

import java.time.LocalDate;
import java.time.LocalTime;

/** Data e horário da primeira aula; as demais são geradas semanalmente pelo servidor. */
public record CriarPedidoRequest(
        Long alunoId,
        Long instrutorId,
        Long pacoteId,
        LocalDate dataPrimeiraAula,
        LocalTime horaInicio
) {}

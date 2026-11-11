package guidaz_api.dto.pedido;

import guidaz_api.model.pedido.PedidoPacoteHorario;
import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioPedidoDTO(LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
    public static HorarioPedidoDTO de(PedidoPacoteHorario h) {
        return new HorarioPedidoDTO(h.getData(), h.getHoraInicio(), h.getHoraFim());
    }
}

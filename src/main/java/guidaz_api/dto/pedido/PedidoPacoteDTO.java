package guidaz_api.dto.pedido;

import guidaz_api.model.pedido.PedidoPacote;
import guidaz_api.model.pedido.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoPacoteDTO(Long id, Long alunoId, Long instrutorId, Long pacoteId,
        String nomePacote, String categoria, Integer quantidadeAulas, BigDecimal precoTotal,
        StatusPedido status, LocalDateTime criadoEm, LocalDateTime comprovanteInformadoEm,
        LocalDateTime analisadoEm, LocalDateTime canceladoEm, String motivoRejeicao,
        List<HorarioPedidoDTO> horarios) {
    public static PedidoPacoteDTO de(PedidoPacote p) {
        return new PedidoPacoteDTO(p.getId(), p.getAluno().getId(), p.getInstrutor().getId(),
            p.getPacoteId(), p.getNomePacote(), p.getCategoria(), p.getQuantidadeAulas(),
            p.getPrecoTotal(), p.getStatus(), p.getCriadoEm(), p.getComprovanteInformadoEm(),
            p.getAnalisadoEm(), p.getCanceladoEm(), p.getMotivoRejeicao(),
            p.getHorarios().stream().map(HorarioPedidoDTO::de).toList());
    }
}

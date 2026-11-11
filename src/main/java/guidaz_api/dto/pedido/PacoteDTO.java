package guidaz_api.dto.pedido;

import guidaz_api.model.pedido.PacoteCatalogo;
import java.math.BigDecimal;

public record PacoteDTO(Long id, String nome, String categoria, Integer quantidadeAulas, BigDecimal preco) {
    public static PacoteDTO de(PacoteCatalogo p) {
        return new PacoteDTO(p.getId(), p.getNome(), p.getCategoria(), p.getQuantidadeAulas(), p.getPreco());
    }
}

package guidaz_api.dto.pedido;

import java.math.BigDecimal;

public record SalvarPacoteRequest(
    Long instrutorId,
    String nome,
    String categoria,
    Integer quantidadeAulas,
    BigDecimal preco
) {}
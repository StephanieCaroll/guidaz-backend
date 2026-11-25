package guidaz_api.dto.pedido;

public record InformarComprovanteRequest(
        Long alunoId,
        String comprovanteUrl,
        String comprovanteNome
) {}
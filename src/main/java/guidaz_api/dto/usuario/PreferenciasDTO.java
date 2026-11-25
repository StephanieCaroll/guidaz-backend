package guidaz_api.dto.usuario;

public record PreferenciasDTO(
        Boolean temaEscuro,
        String tamanhoTexto,
        String idioma,
        Boolean notificacoes
) {
}
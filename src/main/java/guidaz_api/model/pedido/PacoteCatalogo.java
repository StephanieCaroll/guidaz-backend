package guidaz_api.model.pedido;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** Catálogo fixo demonstrativo; IDs não devem ser renumerados depois de publicados. */
public enum PacoteCatalogo {
    A_MOTO(1L, "A / Moto", "A", 2, "220.00"),
    B_CARRO(2L, "B / Carro", "B", 2, "280.00"),
    AB_MOTO_CARRO(3L, "AB / Moto e Carro", "AB", 4, "500.00"),
    C_CAMINHAO(4L, "C / Caminhão", "C", 10, "1500.00"),
    D_ONIBUS(5L, "D / Ônibus", "D", 10, "1400.00"),
    E_CARRETA(6L, "E / Carreta", "E", 15, "2510.00");

    private final Long id;
    private final String nome;
    private final String categoria;
    private final int quantidadeAulas;
    private final BigDecimal preco;

    PacoteCatalogo(Long id, String nome, String categoria, int quantidadeAulas, String preco) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.quantidadeAulas = quantidadeAulas;
        this.preco = new BigDecimal(preco);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public int getQuantidadeAulas() { return quantidadeAulas; }
    public BigDecimal getPreco() { return preco; }

    public static PacoteCatalogo porId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values()).filter(p -> p.id.equals(id)).findFirst().orElse(null);
    }

    public static List<PacoteCatalogo> todos() { return List.of(values()); }
}

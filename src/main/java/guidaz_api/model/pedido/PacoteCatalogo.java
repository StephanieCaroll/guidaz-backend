package guidaz_api.model.pedido;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pacote_catalogo")
public class PacoteCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 10)
    private String categoria;

    @Column(nullable = false)
    private Integer quantidadeAulas;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /** Null = pacote padrão global; preenchido = pacote personalizado de um instrutor. */
    @Column(name = "instrutor_id")
    private Long instrutorId;

    @Column(nullable = false)
    private Boolean ativo = true;

    public PacoteCatalogo() {}

    public PacoteCatalogo(String nome, String categoria, Integer quantidadeAulas, BigDecimal preco, Long instrutorId) {
        this.nome = nome;
        this.categoria = categoria;
        this.quantidadeAulas = quantidadeAulas;
        this.preco = preco;
        this.instrutorId = instrutorId;
        this.ativo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getQuantidadeAulas() { return quantidadeAulas; }
    public void setQuantidadeAulas(Integer quantidadeAulas) { this.quantidadeAulas = quantidadeAulas; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Long getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Long instrutorId) { this.instrutorId = instrutorId; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
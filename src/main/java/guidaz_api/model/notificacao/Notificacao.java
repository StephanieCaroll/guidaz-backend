package guidaz_api.model.notificacao;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destinatario_id", nullable = false)
    private Long destinatarioId;

    @Column(name = "perfil_destinatario", nullable = false, length = 20)
    private String perfilDestinatario;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 300)
    private String descricao;

    /** "PEDIDO" ou "AULA" para navegação direta ao clicar no card. */
    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo;

    /** ID do PedidoPacote ou da Aula relacionada. */
    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private Boolean lida = false;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    public Notificacao() {}

    public Notificacao(
            Long destinatarioId,
            String perfilDestinatario,
            String tipo,
            String titulo,
            String descricao,
            String referenciaTipo,
            Long referenciaId
    ) {
        this.destinatarioId = destinatarioId;
        this.perfilDestinatario = perfilDestinatario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
        this.lida = false;
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }

    public String getPerfilDestinatario() { return perfilDestinatario; }
    public void setPerfilDestinatario(String perfilDestinatario) { this.perfilDestinatario = perfilDestinatario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }

    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
}
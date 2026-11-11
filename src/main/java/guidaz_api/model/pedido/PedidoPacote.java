package guidaz_api.model.pedido;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_pacote")
@Getter
@Setter
@NoArgsConstructor
public class PedidoPacote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;

    @Column(name = "pacote_id", nullable = false)
    private Long pacoteId;

    @Column(name = "nome_pacote", nullable = false, length = 100)
    private String nomePacote;

    @Column(nullable = false, length = 5)
    private String categoria;

    @Column(name = "quantidade_aulas", nullable = false)
    private Integer quantidadeAulas;

    @Column(name = "preco_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "comprovante_informado_em")
    private LocalDateTime comprovanteInformadoEm;

    @Column(name = "analisado_em")
    private LocalDateTime analisadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(name = "motivo_rejeicao", length = 200)
    private String motivoRejeicao;

    @Version
    private Long versao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("data ASC, horaInicio ASC")
    private List<PedidoPacoteHorario> horarios = new ArrayList<>();
}

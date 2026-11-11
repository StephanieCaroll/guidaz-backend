package guidaz_api.model.pedido;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "pedido_pacote_horarios", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pedido_horario_instrutor", columnNames = {"instrutor_id", "data_aula", "hora_inicio", "reserva_ativa"}),
    @UniqueConstraint(name = "uk_pedido_horario_aluno", columnNames = {"aluno_id", "data_aula", "hora_inicio", "reserva_ativa"})
})
@Getter
@Setter
@NoArgsConstructor
public class PedidoPacoteHorario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoPacote pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(name = "data_aula", nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    /** 1 = ocupado; NULL = liberado. MySQL permite múltiplos NULL no índice único. */
    @Column(name = "reserva_ativa")
    private Integer reservaAtiva = 1;
}

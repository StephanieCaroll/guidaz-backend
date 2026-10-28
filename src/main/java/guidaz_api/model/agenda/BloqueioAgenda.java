package guidaz_api.model.agenda;

import guidaz_api.model.usuario.Instrutor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tb_bloqueios_agenda")
@Getter
@Setter
@NoArgsConstructor
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;

    @Column(nullable = false)
    private LocalDate data;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    @Column(nullable = false)
    private boolean diaInteiro = false;

    @Column(length = 200)
    private String motivo;
}
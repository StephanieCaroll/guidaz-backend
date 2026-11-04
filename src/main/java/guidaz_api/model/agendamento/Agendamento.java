package guidaz_api.model.agendamento;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tb_agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data; // Data da aula (ex: 2026-11-10)

    @Column(nullable = false)
    private LocalTime hora; // Hora da aula (ex: 14:00)

    @Column(nullable = false)
    private String categoria; // Categoria da aula (ex: "B")

    // Vincula o aluno que marcou a aula
    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    // Vincula o instrutor que vai dar a aula
    @ManyToOne
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;
}
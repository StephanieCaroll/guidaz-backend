package guidaz_api.model.aula;

import guidaz_api.model.usuario.Aluno;
import guidaz_api.model.usuario.Instrutor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

/** Representa a reserva de um horário de aula entre um aluno e um instrutor. */
@Entity
@Table(name = "agendamentos")
public class Agendamento {

    /** Identificador único do agendamento no banco de dados. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Aluno que realizou a reserva. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    /** Instrutor responsável pela aula reservada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;

    /** Dia em que a aula está marcada. */
    @Column(nullable = false)
    private LocalDate data;

    /** Horário de início da aula. */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /** Horário de término da aula. */
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    /** Construtor padrão necessário para a criação da entidade pelo JPA. */
    public Agendamento() {}

    /** Cria um agendamento preenchendo seus dados e relacionamentos. */
    public Agendamento(Long id, Aluno aluno, Instrutor instrutor, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        this.id = id;
        this.aluno = aluno;
        this.instrutor = instrutor;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public Instrutor getInstrutor() { return instrutor; }
    public void setInstrutor(Instrutor instrutor) { this.instrutor = instrutor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
}

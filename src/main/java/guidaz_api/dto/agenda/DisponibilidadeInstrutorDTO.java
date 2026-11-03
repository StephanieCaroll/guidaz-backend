package guidaz_api.dto.agenda;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class DisponibilidadeInstrutorDTO {
    private Long id;
    private Long instrutorId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Boolean ativo;

    public DisponibilidadeInstrutorDTO() {}

    public DisponibilidadeInstrutorDTO(Long id, Long instrutorId, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim, Boolean ativo) {
        this.id = id;
        this.instrutorId = instrutorId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Long instrutorId) { this.instrutorId = instrutorId; }

    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
package guidaz_api.dto.agenda;

import java.time.LocalDate;
import java.time.LocalTime;

public class BloqueioAgendaDTO {
    private Long id;
    private Long instrutorId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String motivo;

    public BloqueioAgendaDTO() {}

    public BloqueioAgendaDTO(Long id, Long instrutorId, LocalDate data, LocalTime horaInicio, LocalTime horaFim, String motivo) {
        this.id = id;
        this.instrutorId = instrutorId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Long instrutorId) { this.instrutorId = instrutorId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
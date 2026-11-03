package guidaz_api.dto.agenda;

import java.time.LocalTime;

public class SlotDisponivelDTO {
    private LocalTime hora;
    private Boolean disponivel;
    private String status;

    public SlotDisponivelDTO() {}

    public SlotDisponivelDTO(LocalTime hora, Boolean disponivel, String status) {
        this.hora = hora;
        this.disponivel = disponivel;
        this.status = status;
    }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
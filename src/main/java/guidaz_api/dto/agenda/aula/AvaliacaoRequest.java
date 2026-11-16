package guidaz_api.dto.agenda.aula;

import guidaz_api.model.aula.TipoAvaliador;
import lombok.Data;

@Data
public class AvaliacaoRequest {
    private TipoAvaliador tipoAvaliador;
    private Integer nota;
    private String comentario;
}
package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.AulaHistoricoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final AulaService aulaService;

    public List<AulaHistoricoDTO> historicoAluno(Long alunoId) {
        return aulaService.historicoAluno(alunoId);
    }

    public List<AulaHistoricoDTO> historicoInstrutor(Long instrutorId) {
        return aulaService.historicoInstrutor(instrutorId);
    }
}
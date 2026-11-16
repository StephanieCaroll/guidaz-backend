package guidaz_api.service.agenda.aula;

import guidaz_api.dto.agenda.aula.AvaliacaoRequest;
import guidaz_api.exception.DadoInvalidoException;
import guidaz_api.exception.RecursoNaoEncontradoException;
import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.Avaliacao;
import guidaz_api.repository.aula.AulaRepository;
import guidaz_api.repository.aula.AvaliacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AulaRepository aulaRepository;

    @Transactional
    public Avaliacao avaliar(Long aulaId, AvaliacaoRequest request) {
        if (request == null || request.getNota() == null || request.getNota() < 1 || request.getNota() > 5) {
            throw new DadoInvalidoException("A nota deve ser entre 1 e 5.");
        }

        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada."));

        Avaliacao avaliacao = avaliacaoRepository
                .findByAulaIdAndTipoAvaliador(aulaId, request.getTipoAvaliador())
                .orElse(Avaliacao.builder()
                        .aula(aula)
                        .tipoAvaliador(request.getTipoAvaliador())
                        .build());

        avaliacao.setNota(request.getNota());
        avaliacao.setComentario(request.getComentario());

        return avaliacaoRepository.save(avaliacao);
    }
}
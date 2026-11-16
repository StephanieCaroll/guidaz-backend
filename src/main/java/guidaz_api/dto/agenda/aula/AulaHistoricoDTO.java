package guidaz_api.dto.agenda.aula;

import guidaz_api.model.aula.Aula;
import guidaz_api.model.aula.StatusAula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AulaHistoricoDTO {
    private Long id;
    private String titulo;
    private LocalDateTime dataHora;
    private String nomeOutraPessoa;
    private Integer minhaNota;

    private Long alunoId;
    private String alunoNome;
    private String alunoFoto;
    private String alunoTelefone;
    private Long instrutorId;
    private String instrutorNome;
    private String instrutorFoto;
    private String instrutorTelefone;
    private String veiculoModelo;
    private String veiculoPlaca;
    private LocalDate dataAula;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String categoria;
    private String localEncontro;
    private String observacoes;
    private StatusAula status;

    public static AulaHistoricoDTO fromEntity(Aula aula, boolean visaoAluno, Integer minhaNota) {
        String cat = aula.getCategoria() != null && !aula.getCategoria().isBlank()
                ? aula.getCategoria()
                : "B";

        return AulaHistoricoDTO.builder()
                .id(aula.getId())
                .titulo("Aula Prática - Categoria " + cat)
                .dataHora(LocalDateTime.of(aula.getDataAula(), aula.getHoraInicio()))
                .nomeOutraPessoa(visaoAluno ? aula.getInstrutor().getNome() : aula.getAluno().getNome())
                .minhaNota(minhaNota)
                .alunoId(aula.getAluno().getId())
                .alunoNome(aula.getAluno().getNome())
                .alunoFoto(aula.getAluno().getFotoUrl())
                .alunoTelefone(aula.getAluno().getTelefone())
                .instrutorId(aula.getInstrutor().getId())
                .instrutorNome(aula.getInstrutor().getNome())
                .instrutorFoto(aula.getInstrutor().getFotoUrl())
                .instrutorTelefone(aula.getInstrutor().getTelefone())
                .veiculoModelo(aula.getInstrutor().getVeiculoModelo())
                .veiculoPlaca(aula.getInstrutor().getVeiculoPlaca())
                .dataAula(aula.getDataAula())
                .horaInicio(aula.getHoraInicio())
                .horaFim(aula.getHoraFim())
                .categoria(cat)
                .localEncontro(aula.getLocalEncontro())
                .observacoes(aula.getObservacoes())
                .status(aula.getStatus())
                .build();
    }
}
package guidaz_api.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_instrutores")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Instrutor extends Usuario {

    private String categoriasLecionadas;

    private String veiculoModelo;
    private Integer veiculoAno;
    private String veiculoPlaca;
    
    @Column(length = 300)
    private String biografia;
}
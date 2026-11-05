package guidaz_api.model.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String nascimento;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String perfil; 

    @Column(name = "foto_url")
    private String fotoUrl;

    @Lob
    @JsonIgnore
    @Column(name = "foto_data", columnDefinition = "LONGBLOB")
    private byte[] fotoData;

    @Column(name = "foto_type")
    private String fotoType;

    // Endereço
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String numero;
    private String complemento;
}
package br.com.pulsourbano.model.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "zona_cidade")
@SequenceGenerator(name = "seq_zona", sequenceName = "seq_zona", allocationSize = 1)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ZonaCidade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_zona")
    @Column(name = "id_zona")
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 100)
    @Column(length = 100)
    private String municipio = "São Paulo";

    @Embedded
    @Valid
    private Coordenada coordenada;

    private Boolean ativo = true;
}

package br.com.pulsourbano.model.entity;

import br.com.pulsourbano.model.enums.TipoDado;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class LeituraSateliteId implements Serializable {

    @Column(name = "id_zona")
    private Long zonaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dado", length = 30)
    private TipoDado tipoDado;

    @Column(name = "dt_captura")
    private LocalDateTime dtCaptura;
}

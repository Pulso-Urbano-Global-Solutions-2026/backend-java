package br.com.pulsourbano.model.entity;

import br.com.pulsourbano.model.enums.TipoSatelite;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ASSUMPTION: diverge do DDL do CONTEXT.md (que usa PK simples id_leitura).
// Chave composta (zona, tipo_dado, dt_captura) atende rubrica "Modelagem Avançada > chave composta".
// Alinhar com Clayton antes do scheduler (T-29) começar a inserir — ver Q-01.
@Entity
@Table(name = "leitura_satelite")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeituraSatelite {

    @EmbeddedId
    private LeituraSateliteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("zonaId")
    @JoinColumn(name = "id_zona")
    private ZonaCidade zona;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TipoSatelite satelite;

    @Column(columnDefinition = "NUMBER(10,4)")
    private Double valor;

    @Column(length = 20)
    private String unidade;

    @Column(name = "dt_ingestao")
    private LocalDateTime dtIngestao = LocalDateTime.now();
}

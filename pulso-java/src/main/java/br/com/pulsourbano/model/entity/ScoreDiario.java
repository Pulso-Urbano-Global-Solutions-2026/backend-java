package br.com.pulsourbano.model.entity;

import br.com.pulsourbano.model.enums.ClassificacaoScore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "score_diario")
@SequenceGenerator(name = "seq_score", sequenceName = "seq_score", allocationSize = 1)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScoreDiario extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_score")
    @Column(name = "id_score")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona", nullable = false)
    private ZonaCidade zona;

    @Column(name = "dt_score", nullable = false)
    private LocalDate dtScore;

    @Column(name = "valor_score", columnDefinition = "NUMBER(5,2)", nullable = false)
    private Double valorScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private ClassificacaoScore classificacao;

    @Column(name = "no2_valor", columnDefinition = "NUMBER(8,4)")
    private Double no2Valor;

    @Column(name = "temp_valor", columnDefinition = "NUMBER(6,2)")
    private Double tempValor;
}

package br.com.pulsourbano.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recomendacao")
@SequenceGenerator(name = "seq_recomendacao", sequenceName = "seq_recomendacao", allocationSize = 1)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recomendacao extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_recomendacao")
    @Column(name = "id_rec")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_score")
    private ScoreDiario score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(length = 1000, nullable = false)
    private String texto;

    @Column(length = 30)
    private String icone;

    @Column(name = "dt_entrega")
    private LocalDateTime dtEntrega = LocalDateTime.now();
}

package br.com.pulsourbano.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_consulta")
@SequenceGenerator(name = "seq_log", sequenceName = "seq_log", allocationSize = 1)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_log")
    @Column(name = "id_log")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona")
    private ZonaCidade zona;

    @Column(length = 200)
    private String endpoint;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "dt_consulta")
    private LocalDateTime dtConsulta = LocalDateTime.now();
}

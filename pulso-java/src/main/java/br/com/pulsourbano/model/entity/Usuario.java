package br.com.pulsourbano.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "usuario")
@SequenceGenerator(name = "seq_usuario", sequenceName = "seq_usuario", allocationSize = 1)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usuario")
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @Email @NotBlank @Size(max = 200)
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @NotBlank @Size(max = 255)
    @Column(name = "hash_senha", nullable = false, length = 255)
    private String hashSenha;

    @Column(name = "faz_exercicio")
    private Boolean fazExercicio = false;

    @Column(name = "tem_crianca")
    private Boolean temCrianca = false;

    @Column(name = "tem_problema_resp")
    private Boolean temProblemaResp = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role = Role.USER;

    private Boolean ativo = true;

    public enum Role { USER, ADMIN }
}

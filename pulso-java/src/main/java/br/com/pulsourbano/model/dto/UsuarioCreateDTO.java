package br.com.pulsourbano.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCreateDTO(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String senha,
        Boolean fazExercicio,
        Boolean temCrianca,
        Boolean temProblemaResp
) {}

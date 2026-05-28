package br.com.pulsourbano.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(min = 6, max = 100) String senha,
        Boolean fazExercicio,
        Boolean temCrianca,
        Boolean temProblemaResp
) {}

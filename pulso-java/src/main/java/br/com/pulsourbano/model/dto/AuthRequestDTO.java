package br.com.pulsourbano.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String senha
) {}

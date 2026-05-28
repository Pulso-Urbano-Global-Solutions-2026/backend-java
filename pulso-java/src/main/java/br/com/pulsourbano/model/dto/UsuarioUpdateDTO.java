package br.com.pulsourbano.model.dto;

import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        @Size(max = 150) String nome,
        Boolean fazExercicio,
        Boolean temCrianca,
        Boolean temProblemaResp
) {}

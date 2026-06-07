package br.com.pulsourbano.model.dto;

import br.com.pulsourbano.model.enums.ClassificacaoScore;

import java.time.LocalDateTime;

// HATEOAS wrapper (ScoreCurrentResource) será adicionado em T-35.
// Records não podem estender classes em Java.
public record ScoreCurrentResponseDTO(
        Long scoreId,
        Double score,
        ClassificacaoScore classificacao,
        Double no2Ppb,
        Double tempSuperficieC,
        String fonteDadoNo2,
        String fonteDadoTemp,
        LocalDateTime dtDadoOrbital,
        Long zonaId,
        String zonaNome
) {}

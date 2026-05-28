package br.com.pulsourbano.model.dto;

public record ScoreZonaResumoDTO(
        Long id,
        String nome,
        Double score,
        Double lat,
        Double lon
) {}

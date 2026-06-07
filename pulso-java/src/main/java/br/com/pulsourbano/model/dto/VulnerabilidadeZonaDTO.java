package br.com.pulsourbano.model.dto;

public record VulnerabilidadeZonaDTO(
    Long   zonaId,
    String zonaNome,
    Double scoreAmbiental,
    String classificacaoAmbiental,
    Long   populacaoTotal,
    Double percentualIdosos,
    Double percentualCriancas,
    Double densidadeRelativa,
    Double indiceVulnerabilidade,
    String urgencia,
    String descricaoUrgencia,
    Double lat,
    Double lon
) {}

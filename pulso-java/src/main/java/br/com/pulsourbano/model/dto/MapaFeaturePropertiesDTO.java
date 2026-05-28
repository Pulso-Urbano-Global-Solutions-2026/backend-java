package br.com.pulsourbano.model.dto;

public record MapaFeaturePropertiesDTO(
        Long zonaId,
        String zonaNome,
        Double valor,
        String unidade
) {}

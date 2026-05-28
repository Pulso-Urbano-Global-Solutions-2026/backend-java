package br.com.pulsourbano.model.dto;

public record MapaFeatureDTO(
        String type,
        MapaGeometryDTO geometry,
        MapaFeaturePropertiesDTO properties
) {
    public MapaFeatureDTO {
        type = "Feature";
    }
}

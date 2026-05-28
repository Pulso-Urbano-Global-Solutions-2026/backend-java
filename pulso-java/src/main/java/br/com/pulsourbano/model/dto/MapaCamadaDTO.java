package br.com.pulsourbano.model.dto;

import java.time.LocalDate;
import java.util.List;

public record MapaCamadaDTO(
        String type,
        String fonte,
        LocalDate dtCaptura,
        List<MapaFeatureDTO> features
) {
    public MapaCamadaDTO {
        type = "FeatureCollection";
    }
}

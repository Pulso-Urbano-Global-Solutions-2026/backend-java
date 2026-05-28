package br.com.pulsourbano.model.dto;

import java.util.List;

public record MapaGeometryDTO(String type, List<Double> coordinates) {}

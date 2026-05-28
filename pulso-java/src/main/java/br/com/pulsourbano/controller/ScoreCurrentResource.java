package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.ScoreCurrentResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.RepresentationModel;

public class ScoreCurrentResource extends RepresentationModel<ScoreCurrentResource> {

    private final ScoreCurrentResponseDTO data;

    public ScoreCurrentResource(ScoreCurrentResponseDTO data) {
        this.data = data;
    }

    @JsonUnwrapped
    public ScoreCurrentResponseDTO getData() {
        return data;
    }
}

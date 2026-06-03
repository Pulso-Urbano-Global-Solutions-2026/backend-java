package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.ScoreCurrentResponseDTO;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ScoreModelAssembler {

    public ScoreCurrentResource toResource(ScoreCurrentResponseDTO dto) {
        ScoreCurrentResource r = new ScoreCurrentResource(dto);
        r.add(linkTo(methodOn(ScoreController.class).atual(0, 0)).withSelfRel());
        r.add(linkTo(RecomendacaoController.class).withRel("recomendacao"));
        return r;
    }
}

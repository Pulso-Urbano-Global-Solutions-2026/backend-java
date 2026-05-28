package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioModelAssembler {

    public UsuarioResource toResource(UsuarioResponseDTO dto) {
        UsuarioResource r = new UsuarioResource(dto);
        r.add(linkTo(methodOn(UsuarioController.class).buscar(dto.id())).withSelfRel());
        r.add(linkTo(methodOn(UsuarioController.class).listar(null)).withRel("collection"));
        return r;
    }
}

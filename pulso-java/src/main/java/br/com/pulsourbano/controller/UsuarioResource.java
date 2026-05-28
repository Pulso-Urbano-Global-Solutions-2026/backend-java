package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.UsuarioResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.RepresentationModel;

public class UsuarioResource extends RepresentationModel<UsuarioResource> {

    private final UsuarioResponseDTO data;

    public UsuarioResource(UsuarioResponseDTO data) {
        this.data = data;
    }

    @JsonUnwrapped
    public UsuarioResponseDTO getData() {
        return data;
    }
}

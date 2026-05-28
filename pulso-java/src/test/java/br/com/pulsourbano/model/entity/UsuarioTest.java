package br.com.pulsourbano.model.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void usuario_seEmailInvalido_validacaoDeveFalhar() {
        Usuario u = Usuario.builder().nome("Felipe").email("nao-eh-email").hashSenha("hash").build();
        assertThat(validator.validate(u))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void usuario_camposCorretos_validacaoPassa() {
        Usuario u = Usuario.builder().nome("Felipe").email("felipe@fiap.com.br").hashSenha("hashed").build();
        assertThat(validator.validate(u)).isEmpty();
    }
}

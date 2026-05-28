package br.com.pulsourbano.model.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZonaCidadeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void zonaCidade_comCoordenadaInvalida_falhaValidacao() {
        ZonaCidade z = ZonaCidade.builder()
                .nome("Centro").coordenada(new Coordenada(91.0, 0.0)).build();
        assertThat(validator.validate(z)).isNotEmpty();
    }
}

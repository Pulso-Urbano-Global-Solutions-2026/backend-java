package br.com.pulsourbano.model.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void authRequest_emailNulo_violaValidacao() {
        var dto = new AuthRequestDTO(null, "senha123");
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void authRequest_emailInvalido_violaValidacao() {
        var dto = new AuthRequestDTO("nao-email", "senha123");
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void authResponseDTO_of_retornaBearerComToken() {
        var resp = AuthResponseDTO.of("jwt.token.aqui", 86400000L);
        assertThat(resp.tipo()).isEqualTo("Bearer");
        assertThat(resp.token()).isEqualTo("jwt.token.aqui");
        assertThat(resp.expiraEmMs()).isEqualTo(86400000L);
    }
}

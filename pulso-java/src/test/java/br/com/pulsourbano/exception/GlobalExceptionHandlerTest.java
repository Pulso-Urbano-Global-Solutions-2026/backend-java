package br.com.pulsourbano.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFound_retorna404() {
        var r = handler.notFound(new ResourceNotFoundException("usuario 1 não existe"));
        assertThat(r.getStatusCode().value()).isEqualTo(404);
        assertThat(r.getBody().mensagem()).contains("usuario 1");
    }

    @Test
    void conflict_retorna409() {
        var r = handler.conflict(new EmailJaExisteException("felipe@fiap.com"));
        assertThat(r.getStatusCode().value()).isEqualTo(409);
        assertThat(r.getBody().mensagem()).contains("felipe@fiap.com");
    }

    @Test
    void generic_retorna500() {
        var r = handler.generic(new RuntimeException("boom"));
        assertThat(r.getStatusCode().value()).isEqualTo(500);
        assertThat(r.getBody().erro()).isEqualTo("Internal Server Error");
    }
}

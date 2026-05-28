package br.com.pulsourbano.controller;

import br.com.pulsourbano.AbstractIntegrationTest;
import br.com.pulsourbano.model.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

// Requer T-03 concluído (Docker Desktop TCP habilitado) para rodar.
class AuthFlowIT extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate http;

    @Test
    void registroELogin_e2e_retornaToken_ePermiteAcessoProtegido() {
        // 1. POST /register
        var reg = http.postForEntity("/api/v1/auth/register",
                new RegisterRequestDTO("Felipe", "f@fiap.com", "senha123", true, false, false),
                UsuarioResponseDTO.class);
        assertThat(reg.getStatusCode().value()).isEqualTo(201);
        assertThat(reg.getBody().email()).isEqualTo("f@fiap.com");

        // 2. POST /login
        var login = http.postForEntity("/api/v1/auth/login",
                new AuthRequestDTO("f@fiap.com", "senha123"),
                AuthResponseDTO.class);
        assertThat(login.getStatusCode().value()).isEqualTo(200);
        String token = login.getBody().token();
        assertThat(token).isNotBlank();

        // 3. GET /usuario/{id} sem token → 401
        var semToken = http.getForEntity("/api/v1/usuario/" + reg.getBody().id(), String.class);
        assertThat(semToken.getStatusCode().value()).isEqualTo(401);

        // 4. GET /usuario/{id} com token → 200
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        var comToken = http.exchange("/api/v1/usuario/" + reg.getBody().id(),
                HttpMethod.GET, new HttpEntity<>(h), UsuarioResponseDTO.class);
        assertThat(comToken.getStatusCode().value()).isEqualTo(200);
        assertThat(comToken.getBody().email()).isEqualTo("f@fiap.com");
    }

    @Test
    void login_credenciaisErradas_retorna401() {
        var resp = http.postForEntity("/api/v1/auth/login",
                new AuthRequestDTO("inexistente@fiap.com", "errada"),
                String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void register_emailDuplicado_retorna409() {
        http.postForEntity("/api/v1/auth/register",
                new RegisterRequestDTO("A", "dup@fiap.com", "senha123", false, false, false),
                String.class);
        var resp = http.postForEntity("/api/v1/auth/register",
                new RegisterRequestDTO("B", "dup@fiap.com", "senha123", false, false, false),
                String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }
}

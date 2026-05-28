package br.com.pulsourbano.controller;

import br.com.pulsourbano.AbstractIntegrationTest;
import br.com.pulsourbano.model.dto.AuthRequestDTO;
import br.com.pulsourbano.model.dto.AuthResponseDTO;
import br.com.pulsourbano.model.dto.RegisterRequestDTO;
import br.com.pulsourbano.model.entity.Coordenada;
import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.model.entity.ZonaCidade;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import br.com.pulsourbano.service.ScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

// Requer T-03 concluído (Docker Desktop TCP habilitado) para rodar.
// Também requer oracle-init.sql com procedure calcular_score_zona.
class ScoreFlowIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate http;
    @Autowired ZonaCidadeRepository zonaRepo;
    @Autowired ScoreDiarioRepository scoreRepo;
    @Autowired ScoreService scoreService;

    private String jwtToken;
    private ZonaCidade zona;

    @BeforeEach
    void setup() {
        // Registrar e fazer login para obter token
        http.postForEntity("/api/v1/auth/register",
                new RegisterRequestDTO("Tester", "tester@score.com", "senha123", false, false, false),
                String.class);
        var login = http.postForEntity("/api/v1/auth/login",
                new AuthRequestDTO("tester@score.com", "senha123"),
                AuthResponseDTO.class);
        jwtToken = login.getBody().token();

        // Criar zona e popular score via service (procedure real)
        zona = zonaRepo.save(ZonaCidade.builder()
                .nome("Centro SP Score Test")
                .coordenada(new Coordenada(-23.5505, -46.6333))
                .ativo(true).build());
        ScoreDiario score = scoreService.calcularEPersistir(zona, 28.4, 41.2);
        assertThat(score).isNotNull();
    }

    @Test
    void scoreAtual_comToken_retornaScoreComHateoas() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtToken);
        var resp = http.exchange(
                "/api/v1/score/current?lat=-23.5505&lon=-46.6333",
                HttpMethod.GET, new HttpEntity<>(h), String.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        String body = resp.getBody();
        assertThat(body).contains("score");
        assertThat(body).contains("classificacao");
        assertThat(body).contains("_links");
        assertThat(body).contains("self");
        assertThat(body).contains("recomendacao");
    }

    @Test
    void scoreAtual_semToken_retorna401() {
        var resp = http.getForEntity(
                "/api/v1/score/current?lat=-23.5505&lon=-46.6333", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void scoreZonas_semToken_retornaListaPublica() {
        var resp = http.getForEntity("/api/v1/score/zonas", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).contains("zonas");
    }

    @Test
    void scoreHistorico_comToken_retornaItens() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwtToken);
        var resp = http.exchange(
                "/api/v1/score/historico?zonaId=" + zona.getId() + "&dias=7",
                HttpMethod.GET, new HttpEntity<>(h), String.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).contains("historico");
    }
}

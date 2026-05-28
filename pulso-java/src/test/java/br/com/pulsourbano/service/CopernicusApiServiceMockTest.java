package br.com.pulsourbano.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CopernicusApiServiceMockTest {

    private CopernicusApiService service;
    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TOKEN_URL = "http://copernicus-mock/token";
    private static final String OPENMETEO = "https://air-quality-api.open-meteo.com";

    @BeforeEach
    void setUp() {
        service = new CopernicusApiService(mapper);
        ReflectionTestUtils.setField(service, "user",          "test-user");
        ReflectionTestUtils.setField(service, "pass",          "test-pass");
        ReflectionTestUtils.setField(service, "tokenUrl",      TOKEN_URL);
        ReflectionTestUtils.setField(service, "catalogUrl",    "http://copernicus-mock/catalog");
        ReflectionTestUtils.setField(service, "cacheTtlHours", 24L);

        RestTemplate rt = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        mockServer = MockRestServiceServer.createServer(rt);
    }

    @Test
    void buscarNo2_cacheHit_zeroRequestsHttp() throws Exception {
        // Pré-preenche cache — nenhuma chamada HTTP deve ocorrer
        Files.createDirectories(Paths.get("cache"));
        mapper.writeValue(Paths.get("cache/no2_-23.55_-46.63.json").toFile(),
                Map.of("valor", 18.7, "timestamp", Instant.now().toString()));

        double valor = service.buscarNo2(-23.55, -46.63);

        assertThat(valor).isCloseTo(18.7, within(0.01));
        mockServer.verify(); // zero requests esperados — passa se não houve chamadas
    }

    @Test
    void buscarNo2_falhaToken_fallbackOpenMeteo() throws Exception {
        Files.deleteIfExists(Paths.get("cache/no2_-23.10_-46.10.json"));

        // Token falha → try/catch → fallback Open-Meteo
        mockServer.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        mockServer.expect(requestTo(containsString(OPENMETEO)))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(Map.of(
                                "current", Map.of("nitrogen_dioxide", 40.0))),
                        MediaType.APPLICATION_JSON));

        double valor = service.buscarNo2(-23.10, -46.10);

        // 40 µg/m³ × 0.531 = 21.24 ppb
        assertThat(valor).isCloseTo(21.24, within(0.1));
        mockServer.verify();
    }

    @Test
    void buscarNo2_cacheExpirado_revalidaViaFallback() throws Exception {
        // Grava cache com timestamp de 2 dias atrás (expirado)
        Files.createDirectories(Paths.get("cache"));
        Instant expirado = Instant.now().minusSeconds(48 * 3600);
        mapper.writeValue(Paths.get("cache/no2_-23.20_-46.20.json").toFile(),
                Map.of("valor", 99.9, "timestamp", expirado.toString()));

        // Token falha → Open-Meteo
        mockServer.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());
        mockServer.expect(requestTo(containsString(OPENMETEO)))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(Map.of(
                                "current", Map.of("nitrogen_dioxide", 30.0))),
                        MediaType.APPLICATION_JSON));

        double valor = service.buscarNo2(-23.20, -46.20);

        // Não retorna o cache expirado (99.9) — chama API e retorna 30 × 0.531 = 15.93
        assertThat(valor).isNotCloseTo(99.9, within(1.0));
        assertThat(valor).isCloseTo(15.93, within(0.1));
        mockServer.verify();
    }
}

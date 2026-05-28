package br.com.pulsourbano.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CopernicusApiServiceTest {

    private CopernicusApiService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new CopernicusApiService(mapper);
        ReflectionTestUtils.setField(service, "user", "test-user");
        ReflectionTestUtils.setField(service, "pass", "test-pass");
        ReflectionTestUtils.setField(service, "tokenUrl", "http://localhost/token");
        ReflectionTestUtils.setField(service, "catalogUrl", "http://localhost/catalog");
        ReflectionTestUtils.setField(service, "cacheTtlHours", 24L);
    }

    @Test
    void buscarNo2_seCacheValido_retornaDoCache() throws Exception {
        Files.createDirectories(Paths.get("cache"));
        Map<String, Object> cached = Map.of("valor", 22.5, "timestamp", Instant.now().toString());
        mapper.writeValue(Paths.get("cache/no2_-23.55_-46.63.json").toFile(), cached);

        double valor = service.buscarNo2(-23.55, -46.63);
        assertThat(valor).isCloseTo(22.5, within(0.01));
    }
}

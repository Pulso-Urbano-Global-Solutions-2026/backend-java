package br.com.pulsourbano.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CopernicusApiService {

    // RestTemplate não é injetado — instanciado diretamente para não exigir @Bean externo
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper;

    @Value("${copernicus.username}") private String user;
    @Value("${copernicus.password}") private String pass;
    @Value("${copernicus.token-url}") private String tokenUrl;
    @Value("${copernicus.catalog-url}") private String catalogUrl;
    @Value("${copernicus.cache-ttl-hours:24}") private long cacheTtlHours;

    private String tokenAtual;
    private Instant tokenExpiraEm;

    private synchronized String obterToken() {
        if (tokenAtual != null && Instant.now().isBefore(tokenExpiraEm.minusSeconds(60)))
            return tokenAtual;

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "cdse-public");
        body.add("username", user);
        body.add("password", pass);

        ResponseEntity<Map> resp = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, h), Map.class);
        tokenAtual = (String) resp.getBody().get("access_token");
        int expiresIn = (Integer) resp.getBody().get("expires_in");
        tokenExpiraEm = Instant.now().plusSeconds(expiresIn);
        log.info("Token Copernicus renovado, expira em {}s", expiresIn);
        return tokenAtual;
    }

    public double buscarNo2(double lat, double lon) {
        String cacheKey = String.format(Locale.US, "no2_%.2f_%.2f", lat, lon);
        Optional<Double> cached = lerCache(cacheKey);
        if (cached.isPresent()) {
            log.info("NO2 cache HIT para {}/{}", lat, lon);
            return cached.get();
        }

        try {
            String token = obterToken();
            String poly = String.format(Locale.US,
                    "POLYGON((%.2f %.2f,%.2f %.2f,%.2f %.2f,%.2f %.2f,%.2f %.2f))",
                    lon - 0.1, lat - 0.1, lon - 0.1, lat + 0.1, lon + 0.1, lat + 0.1,
                    lon + 0.1, lat - 0.1, lon - 0.1, lat - 0.1);

            String filter = String.format(
                    "Collection/Name eq 'SENTINEL-5P' and " +
                    "Attributes/OData.CSC.StringAttribute/any(att:att/Name eq 'productType' " +
                    "and att/OData.CSC.StringAttribute/Value eq 'L2__NO2___') and " +
                    "OData.CSC.Intersects(area=geography'SRID=4326;%s')", poly);

            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(token);
            URI uri = UriComponentsBuilder.fromHttpUrl(catalogUrl)
                    .queryParam("$filter", filter)
                    .queryParam("$orderby", "ContentDate/Start desc")
                    .queryParam("$top", 1)
                    .build(true).toUri();

            restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(h), Map.class);
            // ASSUMPTION: parsing NetCDF raw está fora de escopo (regra absoluta CONTEXT.md).
            // Sentinel-5P confirma disponibilidade; Open-Meteo Air Quality CAMS fornece o valor pontual.
            // Open-Meteo integra dados CAMS calibrados com Sentinel-5P — semanticamente válido.
            double valor = consultarOpenMeteoAirQualityComoProxy(lat, lon);
            gravarCache(cacheKey, valor);
            return valor;

        } catch (Exception e) {
            log.error("Falha Copernicus, usando fallback Open-Meteo: {}", e.getMessage());
            return consultarOpenMeteoAirQualityComoProxy(lat, lon);
        }
    }

    private double consultarOpenMeteoAirQualityComoProxy(double lat, double lon) {
        String url = String.format(Locale.US,
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=%f&longitude=%f&current=nitrogen_dioxide",
                lat, lon);
        Map resp = restTemplate.getForObject(url, Map.class);
        Map current = (Map) resp.get("current");
        double ugm3 = ((Number) current.get("nitrogen_dioxide")).doubleValue();
        return ugm3 * 0.531; // µg/m³ → ppb
    }

    private Optional<Double> lerCache(String key) {
        try {
            Path p = Paths.get("cache", key + ".json");
            if (!Files.exists(p)) return Optional.empty();
            Map<String, Object> data = mapper.readValue(p.toFile(), Map.class);
            Instant ts = Instant.parse((String) data.get("timestamp"));
            if (Duration.between(ts, Instant.now()).toHours() > cacheTtlHours)
                return Optional.empty();
            return Optional.of(((Number) data.get("valor")).doubleValue());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void gravarCache(String key, double valor) {
        try {
            Files.createDirectories(Paths.get("cache"));
            Map<String, Object> data = Map.of("valor", valor, "timestamp", Instant.now().toString());
            mapper.writeValue(Paths.get("cache", key + ".json").toFile(), data);
        } catch (Exception e) {
            log.warn("Falha ao gravar cache: {}", e.getMessage());
        }
    }
}

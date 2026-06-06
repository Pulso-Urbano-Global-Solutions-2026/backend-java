package br.com.pulsourbano.service;

import br.com.pulsourbano.model.enums.ClassificacaoScore;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class AlertaNetClient {

    private final RestTemplate restTemplate;
    private final String dotnetApiUrl;
    private final String jwtSecret;

    public AlertaNetClient(
            @Value("${dotnet.api.url:http://localhost:5000}") String dotnetApiUrl,
            @Value("${jwt.secret}") String jwtSecret) {
        this.restTemplate = new RestTemplate();
        this.dotnetApiUrl = dotnetApiUrl;
        this.jwtSecret = jwtSecret;
    }

    /** Construtor package-private para testes — permite injetar RestTemplate mockado. */
    AlertaNetClient(RestTemplate restTemplate, String dotnetApiUrl, String jwtSecret) {
        this.restTemplate = restTemplate;
        this.dotnetApiUrl = dotnetApiUrl;
        this.jwtSecret = jwtSecret;
    }

    /**
     * Notifica o .NET API de um alerta ambiental para a zona informada.
     * Só dispara POST para classificações CRITICO ou RUIM.
     * Nunca propaga exceção — falha de notificação não interrompe o pipeline de ingestão.
     */
    public void notificar(Long zonaId, double score, double no2,
                          ClassificacaoScore classificacao, String textoRecomendacao) {

        String nivelAlerta = mapearNivel(classificacao);
        if (nivelAlerta == null) {
            log.debug("Classificação {} não requer notificação ao .NET (zonaId={})",
                    classificacao, zonaId);
            return;
        }

        try {
            String token = gerarTokenServico();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> body = Map.of(
                    "zonaId", zonaId,
                    "nivelAlerta", nivelAlerta,
                    "scoreRegistrado", score,
                    "no2Registrado", no2,
                    "textoRecomendacao", textoRecomendacao
            );

            String url = dotnetApiUrl + "/api/alertas";
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
            log.info("Alerta {} enviado ao .NET para zonaId={} (score={})", nivelAlerta, zonaId, score);

        } catch (RestClientException e) {
            log.warn("Falha ao notificar .NET (zonaId={}, nivel={}): {}", zonaId, nivelAlerta, e.getMessage());
        } catch (Exception e) {
            log.warn("Erro inesperado ao notificar .NET (zonaId={}): {}", zonaId, e.getMessage());
        }
    }

    private String mapearNivel(ClassificacaoScore classificacao) {
        return switch (classificacao) {
            case CRITICO -> "EMERGENCIA";
            case RUIM    -> "ALERTA";
            default      -> null; // BOM e MODERADO não notificam
        };
    }

    private String gerarTokenServico() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .subject("pulso-service")
                .claim("role", "SERVICE")
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + 60_000L))
                .signWith(key)
                .compact();
    }
}

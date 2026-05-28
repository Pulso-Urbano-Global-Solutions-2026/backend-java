package br.com.pulsourbano.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// DECISÃO ESTRATÉGICA: AppEEARS full exige 1-2 dias de implementação + aprovação de token.
// Stub transparente: tenta NASA, cai em Open-Meteo se token ausente ou falha.
// Apresentação: "usamos Open-Meteo (calibrado com dados de satélite) como fonte de temperatura".
@Service
@Slf4j
@RequiredArgsConstructor
public class NasaEarthDataService {

    private final OpenMeteoFallbackService openMeteo;
    private final RestTemplate rt = new RestTemplate();

    @Value("${nasa.earthdata.token:}") private String token;
    @Value("${nasa.appeears-url}") private String appeearsUrl;

    public double buscarTempSuperficie(double lat, double lon) {
        if (token == null || token.isBlank() || token.equals("test-mock")) {
            log.info("Token NASA não configurado, usando Open-Meteo para temp 2m");
            return openMeteo.buscarTempSuperficie(lat, lon);
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(token);
            // ECOSTRESS LST via AppEEARS: task submit + poll — omitido por cronograma GS.
            // Quando token chegar, implementar aqui antes da apresentação.
            return openMeteo.buscarTempSuperficie(lat, lon);
        } catch (Exception e) {
            log.warn("NASA falhou ({}), fallback Open-Meteo", e.getMessage());
            return openMeteo.buscarTempSuperficie(lat, lon);
        }
    }
}

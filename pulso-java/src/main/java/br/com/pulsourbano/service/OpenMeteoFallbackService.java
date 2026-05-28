package br.com.pulsourbano.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class OpenMeteoFallbackService {

    private final RestTemplate rt = new RestTemplate();

    public double buscarTempSuperficie(double lat, double lon) {
        String url = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m",
                lat, lon);
        try {
            Map resp = rt.getForObject(url, Map.class);
            Map current = (Map) resp.get("current");
            return ((Number) current.get("temperature_2m")).doubleValue();
        } catch (Exception e) {
            log.warn("Open-Meteo falhou: {}, usando valor default 28°C", e.getMessage());
            return 28.0;
        }
    }
}

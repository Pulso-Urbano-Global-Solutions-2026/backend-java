package br.com.pulsourbano.service;

import br.com.pulsourbano.model.enums.ClassificacaoScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaNetClientTest {

    // JWT_SECRET deve ter >= 32 bytes para HMAC-SHA256
    private static final String SECRET = "test-secret-com-minimo-256-bits-para-hs256-ok";
    private static final String BASE_URL = "http://localhost:5000";

    @Mock
    private RestTemplate restTemplate;

    private AlertaNetClient client;

    @BeforeEach
    void setUp() {
        client = new AlertaNetClient(restTemplate, BASE_URL, SECRET);
    }

    @Test
    void notificar_scoreCritico_fazPostParaDotNet() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.notificar(1L, 25.0, 40.0, ClassificacaoScore.CRITICO, "Ar CRITICO na zona Centro");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> bodyCaptor =
                ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        verify(restTemplate, times(1))
                .postForEntity(urlCaptor.capture(), bodyCaptor.capture(), eq(Void.class));

        assertThat(urlCaptor.getValue()).contains("/api/alertas");
        assertThat(bodyCaptor.getValue().getBody())
                .containsEntry("nivelAlerta", "EMERGENCIA");
    }

    @Test
    void notificar_scoreRuim_fazPostComNivelAlerta() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.notificar(2L, 55.0, 35.0, ClassificacaoScore.RUIM, "Ar RUIM na zona Norte");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> bodyCaptor =
                ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1))
                .postForEntity(anyString(), bodyCaptor.capture(), eq(Void.class));

        assertThat(bodyCaptor.getValue().getBody())
                .containsEntry("nivelAlerta", "ALERTA");
    }

    @Test
    void notificar_scoreModerado_naoFazPost() {
        client.notificar(3L, 65.0, 28.0, ClassificacaoScore.MODERADO, "Ar MODERADO na zona Sul");

        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void notificar_scoreBom_naoFazPost() {
        client.notificar(4L, 90.0, 22.0, ClassificacaoScore.BOM, "Ar BOM na zona Leste");

        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void notificar_dotnetFora_logaWarnSemPropagar() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatNoException().isThrownBy(() ->
                client.notificar(5L, 20.0, 45.0, ClassificacaoScore.CRITICO, "Ar CRITICO na zona Oeste"));
    }

    @Test
    void notificar_excecaoGenerica_naoPropagarNemAbortar() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThatNoException().isThrownBy(() ->
                client.notificar(6L, 30.0, 42.0, ClassificacaoScore.RUIM, "Ar RUIM na zona ABC"));
    }
}

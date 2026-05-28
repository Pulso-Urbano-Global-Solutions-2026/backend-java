package br.com.pulsourbano.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CoordenadaTest {

    @Test
    void distanciaHaversine_seMesmaCoordenada_retornaZero() {
        Coordenada c = new Coordenada(-23.55, -46.63);
        assertThat(c.distanciaHaversineKm(c)).isCloseTo(0.0, within(0.01));
    }

    @Test
    void distanciaHaversine_SPparaRJ_aproximadamente360km() {
        Coordenada sp = new Coordenada(-23.55, -46.63);
        Coordenada rj = new Coordenada(-22.91, -43.20);
        assertThat(sp.distanciaHaversineKm(rj)).isBetween(355.0, 365.0);
    }
}

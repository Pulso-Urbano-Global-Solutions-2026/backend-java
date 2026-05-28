package br.com.pulsourbano.model.entity;

import br.com.pulsourbano.model.enums.TipoDado;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LeituraSateliteIdTest {

    @Test
    void equals_seMesmaZonaTipoData_saoIguais() {
        LeituraSateliteId a = new LeituraSateliteId(1L, TipoDado.NO2, LocalDateTime.of(2026, 6, 1, 0, 0));
        LeituraSateliteId b = new LeituraSateliteId(1L, TipoDado.NO2, LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_seTipoDiferente_naoSaoIguais() {
        LeituraSateliteId a = new LeituraSateliteId(1L, TipoDado.NO2, LocalDateTime.of(2026, 6, 1, 0, 0));
        LeituraSateliteId b = new LeituraSateliteId(1L, TipoDado.UV, LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(a).isNotEqualTo(b);
    }
}

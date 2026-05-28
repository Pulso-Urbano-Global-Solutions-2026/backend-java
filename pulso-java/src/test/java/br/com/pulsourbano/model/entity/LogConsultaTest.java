package br.com.pulsourbano.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class LogConsultaTest {

    @Test
    void builder_naoLancaExcecao() {
        assertThatNoException().isThrownBy(() ->
                LogConsulta.builder().endpoint("/api/v1/score/current").ipOrigem("127.0.0.1").build());
    }

    @Test
    void noArgConstructor_dtConsultaPreenchidaAutomaticamente() {
        LogConsulta log = new LogConsulta();
        assertThat(log.getDtConsulta()).isNotNull();
    }
}

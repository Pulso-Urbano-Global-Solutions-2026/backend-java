package br.com.pulsourbano;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InfraSmokeIT extends AbstractIntegrationTest {

    @Test
    void oracleContainer_isRunning_andSpringContextLoads() {
        assertThat(ORACLE.isRunning()).isTrue();
        assertThat(ORACLE.getJdbcUrl()).contains("oracle");
    }
}

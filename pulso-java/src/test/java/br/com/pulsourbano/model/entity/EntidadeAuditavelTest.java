package br.com.pulsourbano.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntidadeAuditavelTest {

    @Test
    void classeEhAbstrata_eHerdaCorretamente() {
        assertThat(java.lang.reflect.Modifier.isAbstract(EntidadeAuditavel.class.getModifiers())).isTrue();
    }
}

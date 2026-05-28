package br.com.pulsourbano.model.enums;

import org.junit.jupiter.api.Test;

import static br.com.pulsourbano.model.enums.ClassificacaoScore.*;
import static org.assertj.core.api.Assertions.assertThat;

class ClassificacaoScoreTest {

    @Test
    void from_score80_returnsBOM() {
        assertThat(ClassificacaoScore.from(80)).isEqualTo(BOM);
    }

    @Test
    void from_score59_99_returnsRUIM() {
        assertThat(ClassificacaoScore.from(59.99)).isEqualTo(RUIM);
    }

    @Test
    void from_scoreNegative_returnsCRITICO() {
        assertThat(ClassificacaoScore.from(-1)).isEqualTo(CRITICO);
    }

    @Test
    void from_score100_returnsBOM() {
        assertThat(ClassificacaoScore.from(100)).isEqualTo(BOM);
    }

    @Test
    void from_score60_returnsMODERADO() {
        assertThat(ClassificacaoScore.from(60)).isEqualTo(MODERADO);
    }

    @Test
    void from_score40_returnsRUIM() {
        assertThat(ClassificacaoScore.from(40)).isEqualTo(RUIM);
    }

    @Test
    void from_score39_99_returnsCRITICO() {
        assertThat(ClassificacaoScore.from(39.99)).isEqualTo(CRITICO);
    }

    @Test
    void from_score0_returnsCRITICO() {
        assertThat(ClassificacaoScore.from(0)).isEqualTo(CRITICO);
    }
}

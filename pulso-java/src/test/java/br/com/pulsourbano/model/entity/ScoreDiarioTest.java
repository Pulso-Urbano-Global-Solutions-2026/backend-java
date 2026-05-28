package br.com.pulsourbano.model.entity;

import br.com.pulsourbano.model.enums.ClassificacaoScore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreDiarioTest {

    @Test
    void scoreDiario_classificacaoEnum_serializa() {
        ScoreDiario s = ScoreDiario.builder().valorScore(85.0).classificacao(ClassificacaoScore.BOM).build();
        assertThat(s.getClassificacao()).isEqualTo(ClassificacaoScore.BOM);
    }
}

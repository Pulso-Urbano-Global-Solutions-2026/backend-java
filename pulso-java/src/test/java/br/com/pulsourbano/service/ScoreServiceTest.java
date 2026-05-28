package br.com.pulsourbano.service;

import br.com.pulsourbano.model.enums.ClassificacaoScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreServiceTest {

    private ScoreService service;

    @BeforeEach
    void setUp() {
        service = new ScoreService(null, null, null);
    }

    @Test
    void calcularScore_arBomTempBoa_retornaAltoScore() {
        assertThat(service.calcularScore(0, 20)).isEqualTo(100.0);
    }

    @Test
    void calcularScore_arPessimoTempAlta_retornaBaixoScore() {
        assertThat(service.calcularScore(50, 50)).isEqualTo(0.0);
    }

    @Test
    void calcularScore_valoresMedios() {
        // no2=25 → scoreNo2=0.5; temp=40 → scoreTemp=0.5; total = (0.5*0.6 + 0.5*0.4)*100 = 50.0
        assertThat(service.calcularScore(25, 40)).isEqualTo(50.0);
    }

    @Test
    void calcularScore_no2Acima50_limitadoEmZero() {
        assertThat(service.calcularScore(60, 30)).isLessThanOrEqualTo(40.0);
    }

    @Test
    void classificar_score85_retornaBOM() {
        assertThat(service.classificar(85.0)).isEqualTo(ClassificacaoScore.BOM);
    }
}

package br.com.pulsourbano.service;

import br.com.pulsourbano.AbstractIntegrationTest;
import br.com.pulsourbano.model.entity.Coordenada;
import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.model.entity.ZonaCidade;
import br.com.pulsourbano.model.enums.ClassificacaoScore;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// Requer T-03 concluído (Docker Desktop TCP habilitado) para rodar.
// Também requer que oracle-init.sql contenha a procedure calcular_score_zona.
class ScoreServiceIT extends AbstractIntegrationTest {

    @Autowired ScoreService service;
    @Autowired ZonaCidadeRepository zonaRepo;

    @Test
    @Transactional
    void calcularEPersistir_chamaProcedure_eGravaScore() {
        ZonaCidade z = zonaRepo.save(ZonaCidade.builder()
                .nome("Centro Teste")
                .coordenada(new Coordenada(-23.55, -46.63))
                .ativo(true).build());

        ScoreDiario s = service.calcularEPersistir(z, 28.4, 41.2);

        assertThat(s.getValorScore()).isBetween(50.0, 70.0);
        assertThat(s.getClassificacao()).isIn(ClassificacaoScore.MODERADO, ClassificacaoScore.RUIM);
    }
}

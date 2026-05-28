package br.com.pulsourbano.scheduler;

import br.com.pulsourbano.AbstractIntegrationTest;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

// Requer T-03 concluído (Docker Desktop TCP habilitado) para rodar.
// Também requer zonas cadastradas no banco e cache local com valores de NO2/temp.
class IngestaoOrbitalSchedulerIT extends AbstractIntegrationTest {

    @Autowired IngestaoOrbitalScheduler scheduler;
    @Autowired ScoreDiarioRepository scoreRepo;

    @Test
    void ingerirDadosOrbitais_processaTodasZonas() {
        scheduler.executarManualmente();
        assertThat(scoreRepo.count()).isGreaterThanOrEqualTo(3);
    }
}

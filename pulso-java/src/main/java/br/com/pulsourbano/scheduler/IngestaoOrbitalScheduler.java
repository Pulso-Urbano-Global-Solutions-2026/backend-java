package br.com.pulsourbano.scheduler;

import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.model.entity.ZonaCidade;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import br.com.pulsourbano.service.AlertaNetClient;
import br.com.pulsourbano.service.CopernicusApiService;
import br.com.pulsourbano.service.NasaEarthDataService;
import br.com.pulsourbano.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IngestaoOrbitalScheduler {

    private final CopernicusApiService copernicus;
    private final NasaEarthDataService nasa;
    private final ScoreService scoreService;
    private final ZonaCidadeRepository zonaRepo;
    private final AlertaNetClient alertaNetClient;

    @Scheduled(cron = "0 0 6 * * *", zone = "UTC")
    public void ingerirDadosOrbitais() {
        log.info("Iniciando ingestão orbital diária às {}", Instant.now());
        List<ZonaCidade> zonas = zonaRepo.findByAtivoTrue();
        int sucessos = 0, falhas = 0;

        for (ZonaCidade z : zonas) {
            try {
                double no2  = copernicus.buscarNo2(z.getCoordenada().getLat(), z.getCoordenada().getLon());
                double temp = nasa.buscarTempSuperficie(z.getCoordenada().getLat(), z.getCoordenada().getLon());
                ScoreDiario score = scoreService.calcularEPersistir(z, no2, temp);
                String textoRecomendacao = "Qualidade do ar " + score.getClassificacao().name()
                        + " na zona " + z.getNome();
                alertaNetClient.notificar(z.getId(), score.getValorScore(), no2,
                        score.getClassificacao(), textoRecomendacao);
                sucessos++;
            } catch (Exception e) {
                falhas++;
                log.error("Falha ingestão zona {} ({}): {}", z.getId(), z.getNome(), e.getMessage());
                // continua o loop — não interrompe ingestão das outras zonas
            }
        }
        log.info("Ingestão concluída: {} sucessos, {} falhas", sucessos, falhas);
    }

    // Acionamento manual para demo/apresentação sem aguardar o cron das 6h UTC
    public void executarManualmente() {
        ingerirDadosOrbitais();
    }
}

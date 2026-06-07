package br.com.pulsourbano.config;

import br.com.pulsourbano.model.entity.Coordenada;
import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.model.entity.ZonaCidade;
import br.com.pulsourbano.model.enums.ClassificacaoScore;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Insere as 5 zonas de São Paulo e scores históricos se o banco estiver vazio.
 * Substitui a execução manual do puSCHEMA.sql no Oracle FIAP (Railway).
 * Idempotente: verifica count antes de inserir.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ZonaSeedInitializer implements ApplicationRunner {

    private final ZonaCidadeRepository zonaRepo;
    private final ScoreDiarioRepository scoreRepo;

    private static final List<Object[]> ZONAS = List.of(
        new Object[]{"Centro",     "São Paulo", -23.5505, -46.6333},
        new Object[]{"Zona Leste", "São Paulo", -23.5474, -46.4767},
        new Object[]{"Zona Sul",   "São Paulo", -23.6821, -46.6242},
        new Object[]{"Zona Norte", "São Paulo", -23.4891, -46.6262},
        new Object[]{"Zona Oeste", "São Paulo", -23.5607, -46.7182}
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (zonaRepo.count() > 0) {
            log.info("ZonaSeedInitializer: zonas já existem, pulando seed.");
            return;
        }

        log.info("ZonaSeedInitializer: zona_cidade vazia — inserindo 5 zonas de SP.");
        List<ZonaCidade> zonas = ZONAS.stream()
            .map(d -> ZonaCidade.builder()
                .nome((String) d[0])
                .municipio((String) d[1])
                .coordenada(new Coordenada((Double) d[2], (Double) d[3]))
                .ativo(true)
                .build())
            .toList();
        zonas = zonaRepo.saveAll(zonas);
        log.info("ZonaSeedInitializer: {} zonas inseridas.", zonas.size());

        if (scoreRepo.count() > 0) return;

        log.info("ZonaSeedInitializer: inserindo scores históricos de demonstração.");
        LocalDate hoje = LocalDate.now();

        // Dados representativos: (no2Ppb, tempC) → score calculado conforme algoritmo CLAUDE.md
        double[][] leituras = {
            {42.5, 36.8},  // Centro    → CRITICO  ~35
            {48.9, 38.2},  // Leste     → CRITICO  ~25
            {28.4, 32.9},  // Sul       → MODERADO ~60
            {14.8, 27.1},  // Norte     → BOM      ~82
            {22.3, 31.4}   // Oeste     → MODERADO ~70
        };

        for (int i = 0; i < zonas.size(); i++) {
            ZonaCidade z = zonas.get(i);
            double no2  = leituras[i][0];
            double temp = leituras[i][1];

            // Inserir leitura de ontem e de 7 dias atrás para o histórico
            for (int diasAtras : new int[]{1, 7}) {
                double score = calcular(no2, temp);
                scoreRepo.save(ScoreDiario.builder()
                    .zona(z)
                    .dtScore(hoje.minusDays(diasAtras))
                    .valorScore(score)
                    .classificacao(ClassificacaoScore.from(score))
                    .no2Valor(no2)
                    .tempValor(temp)
                    .build());
            }
        }
        log.info("ZonaSeedInitializer: scores históricos inseridos.");
    }

    private static double calcular(double no2, double temp) {
        double sNo2  = Math.max(0, 1 - (no2 / 50.0));
        double sTemp = Math.max(0, 1 - Math.max(0, (temp - 30.0) / 20.0));
        return Math.round((sNo2 * 0.60 + sTemp * 0.40) * 100 * 10.0) / 10.0;
    }
}

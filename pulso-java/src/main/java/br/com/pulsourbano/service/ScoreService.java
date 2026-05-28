package br.com.pulsourbano.service;

import br.com.pulsourbano.exception.IngestaoException;
import br.com.pulsourbano.exception.ResourceNotFoundException;
import br.com.pulsourbano.model.dto.ScoreZonaResumoDTO;
import br.com.pulsourbano.model.dto.ScoreZonasResponseDTO;
import br.com.pulsourbano.model.entity.*;
import br.com.pulsourbano.model.enums.ClassificacaoScore;
import br.com.pulsourbano.model.enums.TipoDado;
import br.com.pulsourbano.model.enums.TipoSatelite;
import br.com.pulsourbano.repository.LeituraSateliteRepository;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreDiarioRepository scoreRepo;
    private final ZonaCidadeRepository zonaRepo;
    private final LeituraSateliteRepository leituraRepo;

    @PersistenceContext
    private EntityManager em;

    // Algoritmo exato do CONTEXT.md — não alterar sem alinhar com procedure PL/SQL
    public double calcularScore(double no2Ppb, double tempSuperficieC) {
        double scoreNo2 = Math.max(0, 1 - (no2Ppb / 50.0));
        double scoreTemp = Math.max(0, 1 - Math.max(0, (tempSuperficieC - 30.0) / 20.0));
        double scoreTotal = (scoreNo2 * 0.60 + scoreTemp * 0.40) * 100;
        return Math.round(scoreTotal * 10.0) / 10.0;
    }

    public ClassificacaoScore classificar(double score) {
        return ClassificacaoScore.from(score);
    }

    @Transactional
    public ScoreDiario calcularEPersistir(ZonaCidade zona, double no2, double temp) {
        salvarLeitura(zona, TipoDado.NO2, TipoSatelite.SENTINEL_5P, no2, "ppb");
        salvarLeitura(zona, TipoDado.TEMP_SUPERFICIE, TipoSatelite.ECOSTRESS, temp, "°C");

        em.createStoredProcedureQuery("calcular_score_zona")
                .registerStoredProcedureParameter("p_zona_id", Long.class, ParameterMode.IN)
                .setParameter("p_zona_id", zona.getId())
                .execute();

        return scoreRepo.findFirstByZonaIdOrderByDtScoreDesc(zona.getId())
                .orElseThrow(() -> new IngestaoException(
                        "Procedure não gerou score para zona " + zona.getId(), null));
    }

    private void salvarLeitura(ZonaCidade z, TipoDado tipo, TipoSatelite sat, double valor, String unidade) {
        LeituraSatelite l = LeituraSatelite.builder()
                .id(new LeituraSateliteId(z.getId(), tipo, LocalDateTime.now()))
                .zona(z).satelite(sat).valor(valor).unidade(unidade)
                .dtIngestao(LocalDateTime.now()).build();
        leituraRepo.save(l);
    }

    public Optional<ScoreDiario> buscarScoreAtual(double lat, double lon) {
        ZonaCidade maisProxima = encontrarZonaMaisProxima(lat, lon);
        return scoreRepo.findFirstByZonaIdOrderByDtScoreDesc(maisProxima.getId());
    }

    public List<ScoreDiario> buscarHistorico(Long zonaId, int dias) {
        return scoreRepo.findByZonaIdAndDtScoreAfterOrderByDtScoreDesc(
                zonaId, LocalDate.now().minusDays(dias));
    }

    public ScoreZonasResponseDTO listarZonasComScore() {
        List<ScoreZonaResumoDTO> resumos = zonaRepo.findByAtivoTrue().stream()
                .map(z -> {
                    double score = scoreRepo.findFirstByZonaIdOrderByDtScoreDesc(z.getId())
                            .map(ScoreDiario::getValorScore).orElse(0.0);
                    Double lat = z.getCoordenada() != null ? z.getCoordenada().getLat() : null;
                    Double lon = z.getCoordenada() != null ? z.getCoordenada().getLon() : null;
                    return new ScoreZonaResumoDTO(z.getId(), z.getNome(), score, lat, lon);
                }).toList();
        return new ScoreZonasResponseDTO(resumos);
    }

    private ZonaCidade encontrarZonaMaisProxima(double lat, double lon) {
        Coordenada alvo = new Coordenada(lat, lon);
        return zonaRepo.findByAtivoTrue().stream()
                .min(Comparator.comparingDouble(z -> z.getCoordenada().distanciaHaversineKm(alvo)))
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma zona ativa cadastrada"));
    }
}

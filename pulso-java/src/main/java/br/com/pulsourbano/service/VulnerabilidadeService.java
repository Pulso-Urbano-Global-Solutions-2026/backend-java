package br.com.pulsourbano.service;

import br.com.pulsourbano.model.dto.VulnerabilidadeZonaDTO;
import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.model.entity.ZonaCidade;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service @Slf4j @RequiredArgsConstructor
public class VulnerabilidadeService {

    private final ZonaCidadeRepository zonaRepo;
    private final ScoreDiarioRepository scoreRepo;

    private record DemografiaZona(long pop, double idosos, double criancas, double densidade) {}

    // Dados IBGE Censo 2022 — São Paulo. Chave: lowercase sem acento.
    private static final Map<String, DemografiaZona> IBGE = Map.of(
        "centro",      new DemografiaZona(350_000L,   15.8, 12.1, 0.82),
        "zona leste",  new DemografiaZona(3_800_000L, 10.2, 19.4, 0.90),
        "zona sul",    new DemografiaZona(3_000_000L, 13.1, 17.2, 0.75),
        "zona norte",  new DemografiaZona(2_000_000L, 12.4, 17.8, 0.68),
        "zona oeste",  new DemografiaZona(1_200_000L, 14.2, 12.8, 0.55)
    );

    private double calcularIndice(double score, DemografiaZona d) {
        double risco = (100.0 - score) / 100.0;
        double fato  = (d.idosos()    / 100.0) * 0.40
                     + (d.criancas()  / 100.0) * 0.35
                     +  d.densidade()           * 0.25;
        return Math.min(100.0, (risco * 0.60 + fato * 0.40) * 100.0);
    }

    private String urgencia(double i) {
        return i >= 70 ? "CRITICA" : i >= 50 ? "ALTA" : i >= 30 ? "MODERADA" : "BAIXA";
    }

    private String descricao(String u, String nome) {
        return switch (u) {
            case "CRITICA"  -> nome + ": ar poluído + alta concentração de grupos vulneráveis. Ação imediata recomendada.";
            case "ALTA"     -> nome + ": qualidade do ar preocupante para grupos de risco. Monitoramento necessário.";
            case "MODERADA" -> nome + ": situação moderada. Crianças e idosos devem limitar atividades ao ar livre.";
            default         -> nome + ": condições ambientais dentro dos parâmetros aceitáveis.";
        };
    }

    private String normalizar(String s) {
        return s.toLowerCase(Locale.ROOT)
            .replace("ã","a").replace("á","a").replace("â","a")
            .replace("é","e").replace("ê","e").replace("í","i")
            .replace("ó","o").replace("ô","o").replace("ú","u")
            .replace("ç","c").trim();
    }

    public List<VulnerabilidadeZonaDTO> listarPorUrgencia() {
        List<VulnerabilidadeZonaDTO> resultado = new ArrayList<>();

        for (ZonaCidade zona : zonaRepo.findByAtivoTrue()) {
            Optional<ScoreDiario> opt =
                scoreRepo.findFirstByZonaIdOrderByDtScoreDesc(zona.getId());
            if (opt.isEmpty()) { log.warn("Sem score para zona {}", zona.getNome()); continue; }

            ScoreDiario s = opt.get();
            String chave  = normalizar(zona.getNome());

            DemografiaZona d = IBGE.entrySet().stream()
                .filter(e -> chave.contains(e.getKey()) || e.getKey().contains(chave))
                .map(Map.Entry::getValue).findFirst()
                .orElseGet(() -> {
                    log.warn("Sem dados IBGE para '{}' — usando padrao", zona.getNome());
                    return new DemografiaZona(1_000_000L, 12.0, 16.0, 0.70);
                });

            double indice = Math.round(calcularIndice(s.getValorScore(), d) * 10.0) / 10.0;
            String urg    = urgencia(indice);

            resultado.add(new VulnerabilidadeZonaDTO(
                zona.getId(), zona.getNome(),
                s.getValorScore(), s.getClassificacao().name(),
                d.pop(), d.idosos(), d.criancas(), d.densidade(),
                indice, urg, descricao(urg, zona.getNome()),
                zona.getCoordenada() != null ? zona.getCoordenada().getLat() : -23.5505,
                zona.getCoordenada() != null ? zona.getCoordenada().getLon() : -46.6333
            ));
        }

        resultado.sort(Comparator.comparingDouble(VulnerabilidadeZonaDTO::indiceVulnerabilidade).reversed());
        return resultado;
    }
}

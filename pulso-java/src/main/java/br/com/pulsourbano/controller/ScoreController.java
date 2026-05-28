package br.com.pulsourbano.controller;

import br.com.pulsourbano.exception.ResourceNotFoundException;
import br.com.pulsourbano.model.dto.*;
import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.service.ScoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/score")
@RequiredArgsConstructor
@Tag(name = "Score", description = "Score ambiental por coordenada")
public class ScoreController {

    private final ScoreService service;
    private final ScoreModelAssembler assembler;

    @GetMapping("/current")
    public ScoreCurrentResource atual(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lon) {
        ScoreDiario s = service.buscarScoreAtual(lat, lon)
                .orElseThrow(() -> new ResourceNotFoundException("Sem score para coordenadas"));
        ScoreCurrentResponseDTO dto = new ScoreCurrentResponseDTO(
                s.getValorScore(),
                s.getClassificacao(),
                s.getNo2Valor(),
                s.getTempValor(),
                "Sentinel-5P TROPOMI",
                "ECOSTRESS/Open-Meteo",
                s.getDtScore().atStartOfDay(),
                s.getZona().getId(),
                s.getZona().getNome());
        return assembler.toResource(dto);
    }

    @GetMapping("/historico")
    public ScoreHistoricoResponseDTO historico(
            @RequestParam Long zonaId,
            @RequestParam(defaultValue = "7") int dias) {
        return new ScoreHistoricoResponseDTO(zonaId,
                service.buscarHistorico(zonaId, dias).stream()
                        .map(s -> new ScoreHistoricoItemDTO(s.getDtScore(), s.getValorScore(), s.getClassificacao()))
                        .toList());
    }

    @GetMapping("/zonas") // público — sem autenticação (SecurityConfig)
    public ScoreZonasResponseDTO zonas() {
        return service.listarZonasComScore();
    }
}

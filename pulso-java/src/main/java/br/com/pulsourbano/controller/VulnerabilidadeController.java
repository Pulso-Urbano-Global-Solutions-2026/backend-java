package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.VulnerabilidadeZonaDTO;
import br.com.pulsourbano.service.VulnerabilidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vulnerabilidade")
@RequiredArgsConstructor
@Tag(name = "Vulnerabilidade", description = "Indice de vulnerabilidade ambiental — dado orbital x IBGE 2022")
public class VulnerabilidadeController {

    private final VulnerabilidadeService service;

    @GetMapping("/zonas")
    @Operation(summary = "Zonas ordenadas por urgencia: score orbital (60%) + perfil demografico IBGE (40%)")
    public List<VulnerabilidadeZonaDTO> zonasPorUrgencia() {
        return service.listarPorUrgencia();
    }
}

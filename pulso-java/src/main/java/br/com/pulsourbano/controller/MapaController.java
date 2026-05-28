package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.MapaCamadaDTO;
import br.com.pulsourbano.service.MapaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mapa")
@RequiredArgsConstructor
@Tag(name = "Mapa", description = "Camadas GeoJSON de qualidade ambiental")
public class MapaController {

    private final MapaService mapaService;

    @GetMapping("/camadas")
    @Operation(summary = "Camada GeoJSON de NO2 ou temperatura por cidade (público)")
    public MapaCamadaDTO camadas(
            @RequestParam String tipo,
            @RequestParam(defaultValue = "sao_paulo") String cidade) {
        return mapaService.gerarCamada(tipo, cidade);
    }
}

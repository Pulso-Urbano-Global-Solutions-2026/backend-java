package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.RecomendacaoResponseDTO;
import br.com.pulsourbano.service.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recomendacao")
@RequiredArgsConstructor
@Tag(name = "Recomendacao", description = "Recomendação personalizada por perfil de saúde")
public class RecomendacaoController {

    private final RecomendacaoService service;

    @GetMapping
    @Operation(summary = "Gera recomendação personalizada para o score e perfil do usuário")
    public RecomendacaoResponseDTO buscar(
            @RequestParam Long scoreId,
            @RequestParam Long usuarioId) {
        return service.gerar(scoreId, usuarioId);
    }
}

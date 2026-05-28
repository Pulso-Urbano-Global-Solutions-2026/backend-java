package br.com.pulsourbano.model.dto;

import java.util.List;

public record ScoreHistoricoResponseDTO(
        Long usuarioId,
        List<ScoreHistoricoItemDTO> historico
) {}

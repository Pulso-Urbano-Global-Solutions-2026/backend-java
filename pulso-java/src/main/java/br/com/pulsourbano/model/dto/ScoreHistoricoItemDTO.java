package br.com.pulsourbano.model.dto;

import br.com.pulsourbano.model.enums.ClassificacaoScore;

import java.time.LocalDate;

public record ScoreHistoricoItemDTO(
        LocalDate dt,
        Double score,
        ClassificacaoScore classificacao
) {}

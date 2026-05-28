package br.com.pulsourbano.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecomendacaoResponseDTO(
        String texto,
        String icone,
        String nivel,
        List<String> personalizadaPara,
        LocalDateTime dtGeracao
) {}

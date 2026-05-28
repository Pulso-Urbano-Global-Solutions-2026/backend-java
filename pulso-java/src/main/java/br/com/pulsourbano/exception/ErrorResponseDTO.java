package br.com.pulsourbano.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        int status,
        String erro,
        String mensagem,
        List<String> camposInvalidos,
        LocalDateTime timestamp
) {
    public static ErrorResponseDTO of(int status, String erro, String msg) {
        return new ErrorResponseDTO(status, erro, msg, List.of(), LocalDateTime.now());
    }
}

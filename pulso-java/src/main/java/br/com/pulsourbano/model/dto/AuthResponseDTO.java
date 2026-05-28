package br.com.pulsourbano.model.dto;

public record AuthResponseDTO(String token, String tipo, Long expiraEmMs) {

    public static AuthResponseDTO of(String token, long expiraEmMs) {
        return new AuthResponseDTO(token, "Bearer", expiraEmMs);
    }
}

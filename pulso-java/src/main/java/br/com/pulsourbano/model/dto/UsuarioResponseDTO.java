package br.com.pulsourbano.model.dto;

import br.com.pulsourbano.model.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String role,
        Boolean fazExercicio,
        Boolean temCrianca,
        Boolean temProblemaResp,
        LocalDateTime dtCriacao
) {
    public static UsuarioResponseDTO from(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(), u.getNome(), u.getEmail(),
                u.getRole().name(), u.getFazExercicio(), u.getTemCrianca(),
                u.getTemProblemaResp(), u.getDtCriacao());
    }
}

package br.com.pulsourbano.service;

import br.com.pulsourbano.exception.EmailJaExisteException;
import br.com.pulsourbano.exception.ResourceNotFoundException;
import br.com.pulsourbano.model.dto.UsuarioCreateDTO;
import br.com.pulsourbano.model.dto.UsuarioResponseDTO;
import br.com.pulsourbano.model.dto.UsuarioUpdateDTO;
import br.com.pulsourbano.model.entity.Usuario;
import br.com.pulsourbano.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("usuarioService") // nome explícito para SpEL @usuarioService.eDono(...)
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public UsuarioResponseDTO criar(UsuarioCreateDTO dto) {
        if (repo.existsByEmail(dto.email()))
            throw new EmailJaExisteException(dto.email());
        Usuario u = Usuario.builder()
                .nome(dto.nome()).email(dto.email())
                .hashSenha(encoder.encode(dto.senha()))
                .fazExercicio(Boolean.TRUE.equals(dto.fazExercicio()))
                .temCrianca(Boolean.TRUE.equals(dto.temCrianca()))
                .temProblemaResp(Boolean.TRUE.equals(dto.temProblemaResp()))
                .role(Usuario.Role.USER).ativo(true).build();
        return UsuarioResponseDTO.from(repo.save(u));
    }

    public UsuarioResponseDTO buscar(Long id) {
        return UsuarioResponseDTO.from(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + id)));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + id));
        if (dto.nome() != null) u.setNome(dto.nome());
        if (dto.fazExercicio() != null) u.setFazExercicio(dto.fazExercicio());
        if (dto.temCrianca() != null) u.setTemCrianca(dto.temCrianca());
        if (dto.temProblemaResp() != null) u.setTemProblemaResp(dto.temProblemaResp());
        return UsuarioResponseDTO.from(repo.save(u));
    }

    @Transactional
    public void softDelete(Long id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + id));
        u.setAtivo(false);
        repo.save(u);
    }

    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return repo.findAll(pageable).map(UsuarioResponseDTO::from);
    }

    // Helper para @PreAuthorize — verifica se o email autenticado é dono do recurso
    public boolean eDono(Long id, String email) {
        return repo.findById(id)
                .map(u -> u.getEmail().equals(email))
                .orElse(false);
    }
}

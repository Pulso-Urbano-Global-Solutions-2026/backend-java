package br.com.pulsourbano.service;

import br.com.pulsourbano.config.JwtConfig;
import br.com.pulsourbano.exception.EmailJaExisteException;
import br.com.pulsourbano.model.dto.AuthRequestDTO;
import br.com.pulsourbano.model.dto.AuthResponseDTO;
import br.com.pulsourbano.model.dto.RegisterRequestDTO;
import br.com.pulsourbano.model.dto.UsuarioResponseDTO;
import br.com.pulsourbano.model.entity.Usuario;
import br.com.pulsourbano.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtConfig jwt;

    public AuthResponseDTO login(AuthRequestDTO req) {
        Usuario u = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (!encoder.matches(req.senha(), u.getHashSenha()))
            throw new BadCredentialsException("Credenciais inválidas");
        return AuthResponseDTO.of(jwt.gerarToken(u), jwt.getExpirationMs());
    }

    @Transactional
    public UsuarioResponseDTO registrar(RegisterRequestDTO req) {
        if (userRepo.existsByEmail(req.email()))
            throw new EmailJaExisteException(req.email());
        Usuario u = Usuario.builder()
                .nome(req.nome()).email(req.email())
                .hashSenha(encoder.encode(req.senha()))
                .fazExercicio(Boolean.TRUE.equals(req.fazExercicio()))
                .temCrianca(Boolean.TRUE.equals(req.temCrianca()))
                .temProblemaResp(Boolean.TRUE.equals(req.temProblemaResp()))
                .role(Usuario.Role.USER).ativo(true).build();
        return UsuarioResponseDTO.from(userRepo.save(u));
    }
}

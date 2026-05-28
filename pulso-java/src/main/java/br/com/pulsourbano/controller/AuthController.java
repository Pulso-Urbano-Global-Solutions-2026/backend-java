package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.AuthRequestDTO;
import br.com.pulsourbano.model.dto.AuthResponseDTO;
import br.com.pulsourbano.model.dto.RegisterRequestDTO;
import br.com.pulsourbano.model.dto.UsuarioResponseDTO;
import br.com.pulsourbano.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e registro")
public class AuthController {

    private final AuthService auth;

    @PostMapping("/login")
    @Operation(summary = "Autentica usuário e retorna JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @PostMapping("/register")
    @Operation(summary = "Registra novo usuário")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegisterRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.registrar(req));
    }
}

package br.com.pulsourbano.controller;

import br.com.pulsourbano.model.dto.UsuarioCreateDTO;
import br.com.pulsourbano.model.dto.UsuarioResponseDTO;
import br.com.pulsourbano.model.dto.UsuarioUpdateDTO;
import br.com.pulsourbano.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuario")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "CRUD de usuários")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioModelAssembler assembler;

    @PostMapping
    @Operation(summary = "Cria novo usuário (público)")
    public ResponseEntity<UsuarioResource> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.status(201).body(assembler.toResource(service.criar(dto)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca usuário por ID (dono ou ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or @usuarioService.eDono(#id, authentication.name)")
    public UsuarioResource buscar(@PathVariable Long id) {
        return assembler.toResource(service.buscar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados do usuário (apenas dono)")
    @PreAuthorize("@usuarioService.eDono(#id, authentication.name)")
    public UsuarioResponseDTO atualizar(@PathVariable Long id,
                                        @Valid @RequestBody UsuarioUpdateDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete do usuário (apenas dono)")
    @PreAuthorize("@usuarioService.eDono(#id, authentication.name)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários paginados (apenas ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return service.listar(pageable);
    }
}

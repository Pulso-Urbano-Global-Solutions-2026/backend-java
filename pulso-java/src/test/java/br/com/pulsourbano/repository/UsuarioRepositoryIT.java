package br.com.pulsourbano.repository;

import br.com.pulsourbano.AbstractIntegrationTest;
import br.com.pulsourbano.model.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// Requer T-03 concluído (Docker Desktop TCP habilitado) para rodar.
// Enquanto isso, use: mvn test -Dtest=UsuarioRepositoryIT -DskipITs para pular.
class UsuarioRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    UsuarioRepository repo;

    @Test
    @Transactional
    void salva_eRecuperaPorEmail() {
        Usuario u = Usuario.builder()
                .nome("Felipe")
                .email("test@fiap.com")
                .hashSenha("hash")
                .build();
        repo.save(u);
        assertThat(repo.findByEmail("test@fiap.com")).isPresent();
        assertThat(repo.existsByEmail("test@fiap.com")).isTrue();
    }
}

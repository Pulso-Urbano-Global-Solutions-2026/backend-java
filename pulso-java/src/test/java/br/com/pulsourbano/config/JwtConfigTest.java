package br.com.pulsourbano.config;

import br.com.pulsourbano.model.entity.Usuario;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtConfigTest {

    @Test
    void gerarToken_eValidar_retornaClaims() {
        JwtConfig jc = new JwtConfig("a".repeat(64), 60_000L);
        Usuario u = Usuario.builder().email("f@f.com").build();
        u.setId(1L);
        u.setRole(Usuario.Role.USER);
        String token = jc.gerarToken(u);
        Claims c = jc.validar(token);
        assertThat(c.getSubject()).isEqualTo("f@f.com");
        assertThat(c.get("usuarioId", Integer.class)).isEqualTo(1);
    }

    @Test
    void jwtConfig_seSecretCurto_lancaErro() {
        assertThatThrownBy(() -> new JwtConfig("curto", 60_000L))
                .isInstanceOf(IllegalStateException.class);
    }
}

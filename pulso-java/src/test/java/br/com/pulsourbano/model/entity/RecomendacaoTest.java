package br.com.pulsourbano.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class RecomendacaoTest {

    @Test
    void builder_comTexto_naoLancaExcecao() {
        assertThatNoException().isThrownBy(() ->
                Recomendacao.builder().texto("Prefira sair antes das 10h.").build());
    }

    @Test
    void builder_textoPreservado() {
        Recomendacao r = Recomendacao.builder().texto("teste").icone("warning").build();
        assertThat(r.getTexto()).isEqualTo("teste");
        assertThat(r.getIcone()).isEqualTo("warning");
    }
}

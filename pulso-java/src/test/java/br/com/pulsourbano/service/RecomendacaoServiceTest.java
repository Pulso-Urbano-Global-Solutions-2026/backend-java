package br.com.pulsourbano.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RecomendacaoServiceTest {

    private RecomendacaoService service;

    @BeforeEach
    void setUp() {
        service = new RecomendacaoService(mock(br.com.pulsourbano.repository.RecomendacaoRepository.class),
                mock(br.com.pulsourbano.repository.UsuarioRepository.class),
                mock(br.com.pulsourbano.repository.ScoreDiarioRepository.class));
    }

    @Test
    void gerarTexto_scoreCriticoComCrianca_concatena() {
        String t = service.gerarTexto(30, false, true, false);
        assertThat(t).contains("crítica").contains("Crianças");
    }

    @Test
    void gerarTexto_scoreBom_semPersonalizacao() {
        String t = service.gerarTexto(90, false, false, false);
        assertThat(t).contains("Ótimo dia").doesNotContain("corrida").doesNotContain("Crianças");
    }

    @Test
    void gerarTexto_scoreRuimComExercicio_adicionaAviso() {
        String t = service.gerarTexto(45, true, false, false);
        assertThat(t).contains("ruim").contains("corrida e ciclismo");
    }

    @Test
    void gerarTexto_scoreModeradoComProblemaResp_adicionaMascara() {
        String t = service.gerarTexto(65, false, false, true);
        assertThat(t).contains("moderada").contains("máscara");
    }

    @Test
    void gerarTexto_scoreModeradoComExercicio_naoAdicionaAviso() {
        // exercicio aviso só quando score < 60
        String t = service.gerarTexto(65, true, false, false);
        assertThat(t).doesNotContain("corrida e ciclismo");
    }
}

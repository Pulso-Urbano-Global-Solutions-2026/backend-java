package br.com.pulsourbano.service;

import br.com.pulsourbano.exception.ResourceNotFoundException;
import br.com.pulsourbano.repository.LeituraSateliteRepository;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScoreServiceProximityTest {

    private ScoreService service;
    private ZonaCidadeRepository zonaRepo;

    @BeforeEach
    void setUp() {
        ScoreDiarioRepository scoreRepo = mock(ScoreDiarioRepository.class);
        zonaRepo = mock(ZonaCidadeRepository.class);
        LeituraSateliteRepository leituraRepo = mock(LeituraSateliteRepository.class);
        service = new ScoreService(scoreRepo, zonaRepo, leituraRepo);
    }

    @Test
    void encontrarZonaMaisProxima_seNenhumaAtiva_lancaResourceNotFound() {
        when(zonaRepo.findByAtivoTrue()).thenReturn(List.of());
        assertThatThrownBy(() -> service.buscarScoreAtual(-23.55, -46.63))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

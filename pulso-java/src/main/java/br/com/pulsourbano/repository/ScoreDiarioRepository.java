package br.com.pulsourbano.repository;

import br.com.pulsourbano.model.entity.ScoreDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScoreDiarioRepository extends JpaRepository<ScoreDiario, Long> {
    Optional<ScoreDiario> findFirstByZonaIdOrderByDtScoreDesc(Long zonaId);
    List<ScoreDiario> findByZonaIdAndDtScoreAfterOrderByDtScoreDesc(Long zonaId, LocalDate desde);
}

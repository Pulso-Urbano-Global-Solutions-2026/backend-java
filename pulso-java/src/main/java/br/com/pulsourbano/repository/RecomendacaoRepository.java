package br.com.pulsourbano.repository;

import br.com.pulsourbano.model.entity.Recomendacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {
    Optional<Recomendacao> findByScoreIdAndUsuarioId(Long scoreId, Long usuarioId);
}

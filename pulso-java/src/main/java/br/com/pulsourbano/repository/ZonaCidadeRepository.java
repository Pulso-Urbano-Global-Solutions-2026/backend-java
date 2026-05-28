package br.com.pulsourbano.repository;

import br.com.pulsourbano.model.entity.ZonaCidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaCidadeRepository extends JpaRepository<ZonaCidade, Long> {
    List<ZonaCidade> findByAtivoTrue();
}

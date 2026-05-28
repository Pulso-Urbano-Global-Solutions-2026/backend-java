package br.com.pulsourbano.repository;

import br.com.pulsourbano.model.entity.LeituraSatelite;
import br.com.pulsourbano.model.entity.LeituraSateliteId;
import br.com.pulsourbano.model.enums.TipoDado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeituraSateliteRepository extends JpaRepository<LeituraSatelite, LeituraSateliteId> {

    @Query("SELECT l FROM LeituraSatelite l WHERE l.zona.id = :zonaId AND l.id.tipoDado = :tipo ORDER BY l.id.dtCaptura DESC")
    List<LeituraSatelite> findUltimasPorZonaETipo(
            @Param("zonaId") Long zonaId,
            @Param("tipo") TipoDado tipo,
            Pageable pageable);
}

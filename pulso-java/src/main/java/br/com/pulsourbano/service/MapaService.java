package br.com.pulsourbano.service;

import br.com.pulsourbano.model.dto.*;
import br.com.pulsourbano.model.entity.LeituraSatelite;
import br.com.pulsourbano.model.entity.ZonaCidade;
import br.com.pulsourbano.model.enums.TipoDado;
import br.com.pulsourbano.repository.LeituraSateliteRepository;
import br.com.pulsourbano.repository.ZonaCidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapaService {

    private final ZonaCidadeRepository zonaRepo;
    private final LeituraSateliteRepository leituraRepo;

    public MapaCamadaDTO gerarCamada(String tipo, String cidade) {
        TipoDado tipoDado = tipo.equalsIgnoreCase("no2") ? TipoDado.NO2 : TipoDado.TEMP_SUPERFICIE;
        String fonte = tipo.equalsIgnoreCase("no2") ? "Sentinel-5P TROPOMI" : "ECOSTRESS/Open-Meteo";
        String unidade = tipo.equalsIgnoreCase("no2") ? "ppb" : "°C";

        List<ZonaCidade> zonas = zonaRepo.findByAtivoTrue();

        List<MapaFeatureDTO> features = zonas.stream()
                .filter(z -> z.getCoordenada() != null)
                .map(z -> {
                    List<LeituraSatelite> leituras = leituraRepo.findUltimasPorZonaETipo(
                            z.getId(), tipoDado, PageRequest.of(0, 1));
                    double valor = leituras.isEmpty() ? 0.0 : leituras.get(0).getValor();

                    MapaGeometryDTO geometry = new MapaGeometryDTO(
                            "Point",
                            List.of(z.getCoordenada().getLon(), z.getCoordenada().getLat()));
                    MapaFeaturePropertiesDTO props = new MapaFeaturePropertiesDTO(
                            z.getId(), z.getNome(), valor, unidade);
                    return new MapaFeatureDTO(null, geometry, props);
                }).toList();

        return new MapaCamadaDTO(null, fonte, LocalDate.now(), features);
    }
}

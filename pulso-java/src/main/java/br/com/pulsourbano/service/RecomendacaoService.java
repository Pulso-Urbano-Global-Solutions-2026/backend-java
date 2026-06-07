package br.com.pulsourbano.service;

import br.com.pulsourbano.exception.ResourceNotFoundException;
import br.com.pulsourbano.model.dto.RecomendacaoResponseDTO;
import br.com.pulsourbano.model.entity.ScoreDiario;
import br.com.pulsourbano.model.entity.Usuario;
import br.com.pulsourbano.model.enums.ClassificacaoScore;
import br.com.pulsourbano.repository.RecomendacaoRepository;
import br.com.pulsourbano.repository.ScoreDiarioRepository;
import br.com.pulsourbano.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecomendacaoService {

    private final RecomendacaoRepository recRepo;
    private final UsuarioRepository userRepo;
    private final ScoreDiarioRepository scoreRepo;

    @PersistenceContext
    private EntityManager em;

    public RecomendacaoResponseDTO gerar(Long scoreId, Long usuarioId) {
        ScoreDiario score = scoreRepo.findById(scoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Score " + scoreId));
        Usuario u = userRepo.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + usuarioId));

        String texto = gerarTexto(score.getValorScore(),
                Boolean.TRUE.equals(u.getFazExercicio()),
                Boolean.TRUE.equals(u.getTemCrianca()),
                Boolean.TRUE.equals(u.getTemProblemaResp()));

        String icone = switch (score.getClassificacao()) {
            case BOM      -> "check_circle";
            case MODERADO -> "warning";
            case RUIM     -> "error";
            case CRITICO  -> "dangerous";
        };

        List<String> personalizadoPara = new ArrayList<>();
        if (Boolean.TRUE.equals(u.getFazExercicio())) personalizadoPara.add("exercicio_fisico");
        if (Boolean.TRUE.equals(u.getTemCrianca()))   personalizadoPara.add("crianca_em_casa");
        if (Boolean.TRUE.equals(u.getTemProblemaResp())) personalizadoPara.add("problema_respiratorio");

        try {
            em.createStoredProcedureQuery("registrar_recomendacao")
                    .registerStoredProcedureParameter("p_score_id",   Long.class,   ParameterMode.IN)
                    .registerStoredProcedureParameter("p_usuario_id", Long.class,   ParameterMode.IN)
                    .registerStoredProcedureParameter("p_texto",      String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_icone",      String.class, ParameterMode.IN)
                    .setParameter("p_score_id",   scoreId)
                    .setParameter("p_usuario_id", usuarioId)
                    .setParameter("p_texto",      texto)
                    .setParameter("p_icone",      icone)
                    .execute();
        } catch (Exception e) {
            log.warn("registrar_recomendacao procedure indisponível (non-fatal): {}", e.getMessage());
        }

        return new RecomendacaoResponseDTO(texto, icone,
                score.getClassificacao().name(), personalizadoPara, LocalDateTime.now());
    }

    // Package-private para testes unitários diretos
    String gerarTexto(double score, boolean exerc, boolean crianca, boolean resp) {
        String base = switch (ClassificacaoScore.from(score)) {
            case BOM      -> "Ótimo dia para atividades ao ar livre. Qualidade do ar dentro dos limites da OMS.";
            case MODERADO -> "Qualidade do ar moderada. Prefira sair antes das 10h ou após as 17h.";
            case RUIM     -> "Qualidade do ar ruim. Evite esforço físico prolongado ao ar livre.";
            case CRITICO  -> "Qualidade do ar crítica. Recomendamos permanecer em ambientes fechados.";
        };
        StringBuilder sb = new StringBuilder(base);
        if (exerc  && score < 60) sb.append(" Evite corrida e ciclismo entre 11h e 16h.");
        if (crianca && score < 80) sb.append(" Crianças devem ter atividades ao ar livre limitadas hoje.");
        if (resp   && score < 75) sb.append(" Pessoas com asma ou rinite: use máscara se precisar sair.");
        return sb.toString();
    }
}

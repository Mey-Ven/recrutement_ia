package ma.emsi.recrutementia.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.emsi.recrutementia.dto.MatchRequest;
import ma.emsi.recrutementia.dto.MatchResponse;
import ma.emsi.recrutementia.entities.Candidat;
import ma.emsi.recrutementia.entities.MatchResult;
import ma.emsi.recrutementia.repositories.CandidatRepository;
import ma.emsi.recrutementia.repositories.MatchResultRepository;
import ma.emsi.recrutementia.services.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final AiService aiService;
    private final MatchResultRepository matchResultRepository;
    private final CandidatRepository candidatRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public MatchController(AiService aiService,
                           MatchResultRepository matchResultRepository,
                           CandidatRepository candidatRepository) {
        this.aiService = aiService;
        this.matchResultRepository = matchResultRepository;
        this.candidatRepository = candidatRepository;
    }

    // Réponse API: { "result": {...}, "id": 1 }
    public record MatchApiResponse(MatchResponse result, Long id) {}

    @PostMapping
    public ResponseEntity<?> match(@RequestBody MatchRequest req) {
        try {
            // 1) Appel IA
            MatchResponse res = aiService.matchCvOffer(req.getCvText(), req.getOfferText());

            // 2) Récupérer candidat si fourni
            Candidat candidat = null;
            if (req.getCandidatId() != null) {
                candidat = candidatRepository.findById(req.getCandidatId())
                        .orElseThrow(() -> new RuntimeException("Candidat introuvable"));
            }

            // 3) Sauvegarde en DB
            MatchResult entity = MatchResult.builder()
                    .cvText(req.getCvText())
                    .offerText(req.getOfferText())
                    .score(res.getScore())
                    .matchedJson(mapper.writeValueAsString(res.getMatched()))
                    .missingJson(mapper.writeValueAsString(res.getMissing()))
                    .createdAt(LocalDateTime.now())
                    .candidat(candidat)
                    .build();

            MatchResult saved = matchResultRepository.save(entity);

            // 4) Réponse finale
            return ResponseEntity.ok(new MatchApiResponse(res, saved.getId()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Match error: " + e.getMessage());
        }
    }
}
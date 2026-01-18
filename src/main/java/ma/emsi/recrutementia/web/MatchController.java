package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.dto.CandidateRankingResponse;
import ma.emsi.recrutementia.dto.MatchRequest;
import ma.emsi.recrutementia.dto.MatchResponse;
import ma.emsi.recrutementia.entities.Candidat;
import ma.emsi.recrutementia.entities.CvAnalysis;
import ma.emsi.recrutementia.entities.Offer;
import ma.emsi.recrutementia.repositories.CandidatRepository;
import ma.emsi.recrutementia.repositories.CvAnalysisRepository;
import ma.emsi.recrutementia.repositories.OfferRepository;
import ma.emsi.recrutementia.services.MatchingService;
import ma.emsi.recrutementia.services.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchingService matchingService;
    private final RankingService rankingService;
    private final CvAnalysisRepository cvAnalysisRepository;
    private final OfferRepository offerRepository;
    private final CandidatRepository candidatRepository;

    public MatchController(
            MatchingService matchingService,
            RankingService rankingService,
            CvAnalysisRepository cvAnalysisRepository,
            OfferRepository offerRepository,
            CandidatRepository candidatRepository
    ) {
        this.matchingService = matchingService;
        this.rankingService = rankingService;
        this.cvAnalysisRepository = cvAnalysisRepository;
        this.offerRepository = offerRepository;
        this.candidatRepository = candidatRepository;
    }

    @PostMapping
    public ResponseEntity<?> match(@RequestBody MatchRequest request) {
        try {
            CvAnalysis analysis = cvAnalysisRepository.findById(request.getCvAnalysisId())
                    .orElseThrow(() -> new RuntimeException("Analyse CV introuvable"));

            Offer offer = offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> new RuntimeException("Offre introuvable"));

            Candidat candidat = candidatRepository.findById(request.getCandidatId())
                    .orElseThrow(() -> new RuntimeException("Candidat introuvable"));

            MatchResponse response = matchingService.matchCvWithOffer(analysis, offer, candidat);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🎯 NOUVEAU : Classement des candidats pour une offre
    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<?> rankCandidatesByOffer(@PathVariable Long offerId) {
        try {
            List<CandidateRankingResponse> rankings = 
                rankingService.rankCandidatesForOffer(offerId);
            
            return ResponseEntity.ok(rankings);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🎯 NOUVEAU : Recalculer et classer tous les candidats
    @GetMapping("/by-offer/{offerId}/refresh")
    public ResponseEntity<?> refreshAndRankCandidates(@PathVariable Long offerId) {
        try {
            List<CandidateRankingResponse> rankings = 
                rankingService.rankAllCandidatesForOffer(offerId);
            
            return ResponseEntity.ok(rankings);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🎯 NOUVEAU : Statistiques d'une offre
    @GetMapping("/by-offer/{offerId}/stats")
    public ResponseEntity<?> getOfferStats(@PathVariable Long offerId) {
        try {
            RankingService.OfferStatistics stats = 
                rankingService.getOfferStatistics(offerId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
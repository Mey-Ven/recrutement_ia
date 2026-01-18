package ma.emsi.recrutementia.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.emsi.recrutementia.dto.CandidateMatchHistory;
import ma.emsi.recrutementia.dto.CandidateRankingResponse;
import ma.emsi.recrutementia.dto.CandidateStatistics;
import ma.emsi.recrutementia.dto.MatchResponse;
import ma.emsi.recrutementia.entities.Candidat;
import ma.emsi.recrutementia.entities.CvAnalysis;
import ma.emsi.recrutementia.entities.MatchResult;
import ma.emsi.recrutementia.entities.Offer;
import ma.emsi.recrutementia.repositories.CandidatRepository;
import ma.emsi.recrutementia.repositories.CvAnalysisRepository;
import ma.emsi.recrutementia.repositories.MatchResultRepository;
import ma.emsi.recrutementia.repositories.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private final MatchResultRepository matchResultRepository;
    private final OfferRepository offerRepository;
    private final CandidatRepository candidatRepository;
    private final CvAnalysisRepository cvAnalysisRepository;
    private final MatchingService matchingService;
    private final ObjectMapper mapper = new ObjectMapper();

    public RankingService(
            MatchResultRepository matchResultRepository,
            OfferRepository offerRepository,
            CandidatRepository candidatRepository,
            CvAnalysisRepository cvAnalysisRepository,
            MatchingService matchingService
    ) {
        this.matchResultRepository = matchResultRepository;
        this.offerRepository = offerRepository;
        this.candidatRepository = candidatRepository;
        this.cvAnalysisRepository = cvAnalysisRepository;
        this.matchingService = matchingService;
    }

    /**
     * Classement des candidats pour une offre donnée
     * Utilise les résultats déjà calculés en base
     */
    @Transactional(readOnly = true)
    public List<CandidateRankingResponse> rankCandidatesForOffer(Long offerId) throws Exception {
        
        List<MatchResult> results = matchResultRepository.findByOfferIdOrderByScoreDesc(offerId);
        
        List<CandidateRankingResponse> rankings = new ArrayList<>();
        
        for (MatchResult result : results) {
            Candidat candidat = result.getCandidat();
            
            Set<String> matched = mapper.readValue(
                result.getMatchedJson(), 
                new TypeReference<Set<String>>() {}
            );
            
            Set<String> missing = mapper.readValue(
                result.getMissingJson(), 
                new TypeReference<Set<String>>() {}
            );
            
            CandidateRankingResponse ranking = new CandidateRankingResponse(
                candidat.getId(),
                candidat.getNom() + " " + candidat.getPrenom(),
                candidat.getEmail(),
                result.getScore(),
                new ArrayList<>(matched),
                new ArrayList<>(missing),
                result.getCreatedAt()
            );
            
            rankings.add(ranking);
        }
        
        return rankings;
    }

    /**
     * Recalculer en temps réel pour tous les candidats
     */
    @Transactional(readOnly = true)
    public List<CandidateRankingResponse> rankAllCandidatesForOffer(Long offerId) throws Exception {
        
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
        
        List<Candidat> allCandidats = candidatRepository.findAll();
        
        List<CandidateRankingResponse> rankings = new ArrayList<>();
        
        for (Candidat candidat : allCandidats) {
            Optional<CvAnalysis> analysisOpt = cvAnalysisRepository
                    .findTopByCandidatIdOrderByAnalyzedAtDesc(candidat.getId());
            
            if (analysisOpt.isEmpty()) {
                continue;
            }
            
            CvAnalysis analysis = analysisOpt.get();
            
            MatchResponse matchResponse = matchingService.matchCvWithOffer(
                analysis, 
                offer, 
                candidat
            );
            
            CandidateRankingResponse ranking = new CandidateRankingResponse(
                candidat.getId(),
                candidat.getNom() + " " + candidat.getPrenom(),
                candidat.getEmail(),
                matchResponse.getScore(),
                matchResponse.getMatched(),
                matchResponse.getMissing(),
                analysis.getAnalyzedAt()
            );
            
            rankings.add(ranking);
        }
        
        rankings.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        
        return rankings;
    }

    /**
     * Statistiques pour une offre
     */
    @Transactional(readOnly = true)
    public OfferStatistics getOfferStatistics(Long offerId) throws Exception {
        List<CandidateRankingResponse> rankings = rankCandidatesForOffer(offerId);
        
        if (rankings.isEmpty()) {
            return new OfferStatistics(0, 0, 0, 0, 0.0);
        }
        
        int total = rankings.size();
        long highlyRecommended = rankings.stream()
                .filter(r -> r.getScore() >= 75)
                .count();
        long toConsider = rankings.stream()
                .filter(r -> r.getScore() >= 50 && r.getScore() < 75)
                .count();
        long notQualified = rankings.stream()
                .filter(r -> r.getScore() < 50)
                .count();
        double averageScore = rankings.stream()
                .mapToInt(CandidateRankingResponse::getScore)
                .average()
                .orElse(0.0);
        
        return new OfferStatistics(
            total, 
            (int) highlyRecommended, 
            (int) toConsider, 
            (int) notQualified, 
            averageScore
        );
    }
    
    // ========================================
    // 🆕 NOUVELLES MÉTHODES POUR L'HISTORIQUE
    // ========================================
    
    /**
     * Récupère l'historique complet des matchings d'un candidat
     */
    @Transactional(readOnly = true)
    public List<CandidateMatchHistory> getCandidateMatchHistory(Long candidatId) {
        // Vérifier que le candidat existe
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));
        
        // Récupérer tous les matchings du candidat (triés par date décroissante)
        List<MatchResult> matchResults = matchResultRepository
            .findByCandidatIdOrderByCreatedAtDesc(candidatId);
        
        // Mapper vers le DTO d'historique
        return matchResults.stream().map(match -> {
            try {
                // Trouver l'offre correspondante
                Offer offer = offerRepository.findByDescription(match.getOfferText())
                    .orElse(null);
                
                // Parser les JSON
                List<String> matched = mapper.readValue(
                    match.getMatchedJson(), 
                    new TypeReference<List<String>>() {}
                );
                
                List<String> missing = mapper.readValue(
                    match.getMissingJson(), 
                    new TypeReference<List<String>>() {}
                );
                
                return CandidateMatchHistory.builder()
                    .matchId(match.getId())
                    .offerId(offer != null ? offer.getId() : null)
                    .offerTitle(offer != null ? offer.getTitre() : "Offre supprimée")
                    .score(match.getScore())
                    .matched(matched)
                    .missing(missing)
                    .matchedAt(match.getCreatedAt())
                    .status("En attente") // À adapter selon votre logique métier
                    .build();
                    
            } catch (Exception e) {
                System.err.println("Erreur parsing JSON pour match #" + match.getId());
                return null;
            }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
    
    /**
     * Récupère uniquement les meilleurs matchings (dernier par offre)
     */
    @Transactional(readOnly = true)
    public List<CandidateMatchHistory> getCandidateBestMatches(Long candidatId) {
        List<CandidateMatchHistory> allHistory = getCandidateMatchHistory(candidatId);
        
        // Garder uniquement le dernier matching par offre
        Map<Long, CandidateMatchHistory> bestMatchesByOffer = new HashMap<>();
        
        for (CandidateMatchHistory history : allHistory) {
            Long offerId = history.getOfferId();
            if (offerId != null) {
                // Si pas encore d'entrée OU si ce match est plus récent
                if (!bestMatchesByOffer.containsKey(offerId) ||
                    history.getMatchedAt().isAfter(
                        bestMatchesByOffer.get(offerId).getMatchedAt()
                    )) {
                    bestMatchesByOffer.put(offerId, history);
                }
            }
        }
        
        // Retourner triés par score décroissant
        return bestMatchesByOffer.values().stream()
            .sorted(Comparator.comparing(CandidateMatchHistory::getScore).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Calcule les statistiques d'un candidat
     */
    @Transactional(readOnly = true)
    public CandidateStatistics getCandidateStatistics(Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId)
            .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + candidatId));
        
        List<CandidateMatchHistory> history = getCandidateBestMatches(candidatId);
        
        if (history.isEmpty()) {
            return CandidateStatistics.builder()
                .candidatId(candidatId)
                .candidatName(candidat.getNom() + " " + candidat.getPrenom())
                .candidatEmail(candidat.getEmail())
                .totalMatches(0)
                .excellentMatches(0)
                .goodMatches(0)
                .partialMatches(0)
                .lowMatches(0)
                .averageScore(0.0)
                .build();
        }
        
        // Calculs statistiques
        int excellent = (int) history.stream().filter(h -> h.getScore() >= 75).count();
        int good = (int) history.stream().filter(h -> h.getScore() >= 50 && h.getScore() < 75).count();
        int partial = (int) history.stream().filter(h -> h.getScore() >= 25 && h.getScore() < 50).count();
        int low = (int) history.stream().filter(h -> h.getScore() < 25).count();
        
        double avgScore = history.stream()
            .mapToInt(CandidateMatchHistory::getScore)
            .average()
            .orElse(0.0);
        
        Optional<CandidateMatchHistory> bestMatch = history.stream()
            .max(Comparator.comparing(CandidateMatchHistory::getScore));
        
        Optional<CandidateMatchHistory> worstMatch = history.stream()
            .min(Comparator.comparing(CandidateMatchHistory::getScore));
        
        return CandidateStatistics.builder()
            .candidatId(candidatId)
            .candidatName(candidat.getNom() + " " + candidat.getPrenom())
            .candidatEmail(candidat.getEmail())
            .totalMatches(history.size())
            .excellentMatches(excellent)
            .goodMatches(good)
            .partialMatches(partial)
            .lowMatches(low)
            .averageScore(Math.round(avgScore * 100.0) / 100.0)
            .bestScore(bestMatch.map(CandidateMatchHistory::getScore).orElse(null))
            .worstScore(worstMatch.map(CandidateMatchHistory::getScore).orElse(null))
            .bestMatchOfferId(bestMatch.map(CandidateMatchHistory::getOfferId).orElse(null))
            .bestMatchOfferTitle(bestMatch.map(CandidateMatchHistory::getOfferTitle).orElse(null))
            .build();
    }
    
    // Classe interne existante
    public static class OfferStatistics {
        private int totalCandidates;
        private int highlyRecommended;
        private int toConsider;
        private int notQualified;
        private double averageScore;
        
        public OfferStatistics(int totalCandidates, int highlyRecommended, 
                             int toConsider, int notQualified, double averageScore) {
            this.totalCandidates = totalCandidates;
            this.highlyRecommended = highlyRecommended;
            this.toConsider = toConsider;
            this.notQualified = notQualified;
            this.averageScore = averageScore;
        }
        
        public int getTotalCandidates() { return totalCandidates; }
        public int getHighlyRecommended() { return highlyRecommended; }
        public int getToConsider() { return toConsider; }
        public int getNotQualified() { return notQualified; }
        public double getAverageScore() { return averageScore; }
    }
}
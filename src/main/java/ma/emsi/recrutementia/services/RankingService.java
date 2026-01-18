package ma.emsi.recrutementia.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.emsi.recrutementia.dto.CandidateRankingResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

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
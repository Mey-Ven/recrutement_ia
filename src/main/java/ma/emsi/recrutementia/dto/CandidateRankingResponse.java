package ma.emsi.recrutementia.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CandidateRankingResponse {
    
    private Long candidatId;
    private String candidatName;
    private String candidatEmail;
    private Integer score;
    private List<String> matched;
    private List<String> missing;
    private LocalDateTime matchedAt;
    private String recommendation;
    
    public CandidateRankingResponse() {}
    
    public CandidateRankingResponse(Long candidatId, String candidatName, String candidatEmail, 
                                   Integer score, List<String> matched, List<String> missing, 
                                   LocalDateTime matchedAt) {
        this.candidatId = candidatId;
        this.candidatName = candidatName;
        this.candidatEmail = candidatEmail;
        this.score = score;
        this.matched = matched;
        this.missing = missing;
        this.matchedAt = matchedAt;
        this.recommendation = determineRecommendation(score);
    }
    
    private String determineRecommendation(Integer score) {
        if (score >= 75) return "Fortement recommandé";
        if (score >= 50) return "À considérer";
        return "Non qualifié";
    }
    
    // Getters & Setters
    public Long getCandidatId() { return candidatId; }
    public void setCandidatId(Long candidatId) { this.candidatId = candidatId; }
    
    public String getCandidatName() { return candidatName; }
    public void setCandidatName(String candidatName) { this.candidatName = candidatName; }
    
    public String getCandidatEmail() { return candidatEmail; }
    public void setCandidatEmail(String candidatEmail) { this.candidatEmail = candidatEmail; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { 
        this.score = score;
        this.recommendation = determineRecommendation(score);
    }
    
    public List<String> getMatched() { return matched; }
    public void setMatched(List<String> matched) { this.matched = matched; }
    
    public List<String> getMissing() { return missing; }
    public void setMissing(List<String> missing) { this.missing = missing; }
    
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
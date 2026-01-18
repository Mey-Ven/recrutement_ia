package ma.emsi.recrutementia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques d'un candidat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateStatistics {
    
    private Long candidatId;
    private String candidatName;
    private String candidatEmail;
    
    // Statistiques des matchings
    private int totalMatches;
    private int excellentMatches;  // score >= 75%
    private int goodMatches;        // score 50-74%
    private int partialMatches;     // score 25-49%
    private int lowMatches;         // score < 25%
    
    private double averageScore;
    private Integer bestScore;
    private Integer worstScore;
    
    // Offre la mieux matchée
    private Long bestMatchOfferId;
    private String bestMatchOfferTitle;
    
    /**
     * Calcule le taux de succès (matches >= 50%)
     */
    public double getSuccessRate() {
        if (totalMatches == 0) return 0.0;
        return ((excellentMatches + goodMatches) * 100.0) / totalMatches;
    }
}
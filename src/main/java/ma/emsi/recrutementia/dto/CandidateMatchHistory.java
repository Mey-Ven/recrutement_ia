package ma.emsi.recrutementia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO représentant un élément de l'historique de matching d'un candidat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateMatchHistory {
    
    private Long matchId;
    private Long offerId;
    private String offerTitle;
    private Integer score;
    private List<String> matched;
    private List<String> missing;
    private LocalDateTime matchedAt;
    private String status; // "En attente", "Accepté", "Refusé", "En cours"
    
    /**
     * Génère une évaluation textuelle du match
     */
    public String getMatchQuality() {
        if (score >= 75) {
            return "Excellent match";
        } else if (score >= 50) {
            return "Bon match";
        } else if (score >= 25) {
            return "Match partiel";
        } else {
            return "Faible compatibilité";
        }
    }
    
    /**
     * Calcule le nombre de compétences matchées
     */
    public int getMatchedCount() {
        return matched != null ? matched.size() : 0;
    }
    
    /**
     * Calcule le nombre de compétences manquantes
     */
    public int getMissingCount() {
        return missing != null ? missing.size() : 0;
    }
}
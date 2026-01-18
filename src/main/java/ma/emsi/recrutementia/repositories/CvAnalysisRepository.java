package ma.emsi.recrutementia.repositories;

import ma.emsi.recrutementia.entities.CvAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CvAnalysisRepository extends JpaRepository<CvAnalysis, Long> {
    
    // Trouver l'analyse la plus récente d'un candidat
    Optional<CvAnalysis> findTopByCandidatIdOrderByAnalyzedAtDesc(Long candidatId);
}
	
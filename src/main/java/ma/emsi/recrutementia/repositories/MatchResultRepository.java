package ma.emsi.recrutementia.repositories;

import ma.emsi.recrutementia.entities.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    
    // Trouver tous les résultats pour une offre (triés par score DESC)
	@Query("SELECT mr FROM MatchResult mr " +
		       "JOIN FETCH mr.candidat c " +
		       "WHERE mr.offerText = " +
		       "(SELECT o.description FROM Offer o WHERE o.id = :offerId) " +
		       "AND mr.id IN (" +
		       "  SELECT MAX(mr2.id) FROM MatchResult mr2 " +
		       "  WHERE mr2.offerText = mr.offerText " +
		       "  GROUP BY mr2.candidat.id" +
		       ") " +
		       "ORDER BY mr.score DESC, mr.createdAt DESC")
		List<MatchResult> findByOfferIdOrderByScoreDesc(@Param("offerId") Long offerId);
    
    // Trouver tous les résultats d'un candidat
    List<MatchResult> findByCandidatIdOrderByScoreDesc(Long candidatId);
}
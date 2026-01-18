package ma.emsi.recrutementia.repositories;

import ma.emsi.recrutementia.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    
    // ⬇️ NOUVELLE MÉTHODE AJOUTÉE ⬇️
    /**
     * Trouve une offre par sa description exacte
     * Utile pour retrouver l'offre correspondant à un MatchResult
     */
    Optional<Offer> findByDescription(String description);
}
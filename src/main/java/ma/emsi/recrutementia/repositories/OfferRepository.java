package ma.emsi.recrutementia.repositories;

import ma.emsi.recrutementia.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
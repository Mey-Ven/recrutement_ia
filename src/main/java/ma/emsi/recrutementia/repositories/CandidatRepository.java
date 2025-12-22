package ma.emsi.recrutementia.repositories;

import ma.emsi.recrutementia.entities.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {
}
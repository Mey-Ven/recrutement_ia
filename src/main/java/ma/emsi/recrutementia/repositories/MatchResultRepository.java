package ma.emsi.recrutementia.repositories;

import ma.emsi.recrutementia.entities.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
}
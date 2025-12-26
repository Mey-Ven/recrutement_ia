package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.entities.Candidat;
import ma.emsi.recrutementia.entities.MatchResult;
import ma.emsi.recrutementia.repositories.CandidatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidats")
public class CandidatRestController {

    private final CandidatRepository candidatRepository;

    public CandidatRestController(CandidatRepository candidatRepository) {
        this.candidatRepository = candidatRepository;
    }

    @GetMapping
    public List<Candidat> getAll() {
        return candidatRepository.findAll();
    }

    @PostMapping
    public Candidat save(@RequestBody Candidat c) {
        return candidatRepository.save(c);
    }

    // Option 1 : récupérer tous les match results d’un candidat
    @GetMapping("/{id}/matches")
    public ResponseEntity<?> getMatches(@PathVariable Long id) {
        return ResponseEntity.ok(
                candidatRepository.findById(id)
                        .map(Candidat::getMatches)
                        .orElseThrow(() -> new RuntimeException("Candidat introuvable"))
        );
    }
}
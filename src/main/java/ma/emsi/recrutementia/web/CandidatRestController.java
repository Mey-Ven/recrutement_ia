package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.entities.Candidat;
import ma.emsi.recrutementia.repositories.CandidatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidats")
public class CandidatRestController {

    private final CandidatRepository candidatRepository;

    @Autowired
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
}
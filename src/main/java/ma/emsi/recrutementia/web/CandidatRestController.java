package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.entities.Candidat;
import ma.emsi.recrutementia.entities.CvAnalysis;
import ma.emsi.recrutementia.entities.MatchResult;
import ma.emsi.recrutementia.repositories.CandidatRepository;
import ma.emsi.recrutementia.repositories.CvAnalysisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import ma.emsi.recrutementia.services.AiService;
import ma.emsi.recrutementia.services.OcrService;
import ma.emsi.recrutementia.utils.OcrTextCleaner;
import ma.emsi.recrutementia.dto.SkillsResponse;


@RestController
@RequestMapping("/api/candidats")
@CrossOrigin(origins = "*")
public class CandidatRestController {

    private final CandidatRepository candidatRepository;
    private final AiService aiService;
    private final OcrService ocrService;
    
    @Autowired
    private CvAnalysisRepository cvAnalysisRepository;

    public CandidatRestController(
            CandidatRepository candidatRepository,
            AiService aiService,
            OcrService ocrService
    ) {
        this.candidatRepository = candidatRepository;
        this.aiService = aiService;
        this.ocrService = ocrService;
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
    
    @PostMapping("/{id}/cv")
    public ResponseEntity<?> uploadAndAnalyzeCv(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        // 1️⃣ Récupérer le candidat
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat introuvable"));

        // 2️⃣ OCR
        String rawText = ocrService.extractText(file);

        // 3️⃣ Nettoyage
        String cleanText = OcrTextCleaner.clean(rawText);

        // 4️⃣ Extraction des compétences (IA)
        SkillsResponse skillsResponse = aiService.extractSkills(cleanText);

        // 5️⃣ Sauvegarde en base (cv_analysis)
        CvAnalysis analysis = CvAnalysis.builder()
                .candidat(candidat)
                .cvText(cleanText)
                .skillsJson(
                        new ObjectMapper().writeValueAsString(skillsResponse.getSkills())
                )
                .analyzedAt(LocalDateTime.now())
                .build();

        cvAnalysisRepository.save(analysis);

        // 6️⃣ Réponse API
        return ResponseEntity.ok(
                Map.of(
                        "candidateId", candidat.getId(),
                        "analysisId", analysis.getId(),
                        "skills", skillsResponse.getSkills()
                )
        );
    }

    
}
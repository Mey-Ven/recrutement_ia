package ma.emsi.recrutementia.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.emsi.recrutementia.dto.MatchResponse;
import ma.emsi.recrutementia.dto.SkillsResponse;
import ma.emsi.recrutementia.entities.*;
import ma.emsi.recrutementia.repositories.MatchResultRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final MatchResultRepository matchResultRepository;
    private final AiService aiService;

    public MatchingService(
            MatchResultRepository matchResultRepository,
            AiService aiService
    ) {
        this.matchResultRepository = matchResultRepository;
        this.aiService = aiService;
    }

    public MatchResponse matchCvWithOffer(
            CvAnalysis analysis,
            Offer offer,
            Candidat candidat
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        /* =========================
           1️⃣ Skills CV (déjà extraits)
           ========================= */
        Set<String> cvSkills =
                mapper.readValue(
                        analysis.getSkillsJson(),
                        new TypeReference<Set<String>>() {}
                );

        // normalisation CV
        Set<String> cvSkillsNorm = cvSkills.stream()
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        /* =========================
           2️⃣ Skills Offre - EXTRACTION PAR IA
           ========================= */
        SkillsResponse offerSkillsResponse = aiService.extractSkills(offer.getDescription());
        
        Set<String> offerSkills = (offerSkillsResponse.getSkills() == null 
                ? new HashSet<>() 
                : offerSkillsResponse.getSkills().stream()
                        .map(this::normalize)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toSet())
        );

        // Log pour debugging
        System.out.println("=== EXTRACTION SKILLS ===");
        System.out.println("Description offre: " + offer.getDescription());
        System.out.println("Skills CV: " + cvSkillsNorm);
        System.out.println("Skills Offre (extraits par IA): " + offerSkills);
        System.out.println("========================");

        // Si aucun skill dans l'offre, retourner score 0
        if (offerSkills.isEmpty()) {
            return createEmptyResponse(analysis, offer, candidat, mapper);
        }

        /* =========================
           3️⃣ Matching INTELLIGENT
           ========================= */
        Set<String> matchedCvSkills = new HashSet<>();
        Set<String> matchedOfferSkills = new HashSet<>();

        for (String cvSkill : cvSkillsNorm) {
            for (String offerSkill : offerSkills) {
                if (isMatch(cvSkill, offerSkill)) {
                    matchedCvSkills.add(cvSkill);
                    matchedOfferSkills.add(offerSkill);
                }
            }
        }

        // Skills manquants = skills de l'offre NON matchés
        Set<String> missing = new HashSet<>(offerSkills);
        missing.removeAll(matchedOfferSkills);

        /* =========================
           4️⃣ Score
           ========================= */
        int score = (int) ((matchedOfferSkills.size() * 100.0) / offerSkills.size());

        System.out.println("=== RESULTAT MATCHING ===");
        System.out.println("Matched: " + matchedCvSkills);
        System.out.println("Missing: " + missing);
        System.out.println("Score: " + score + "%");
        System.out.println("=========================");

        /* =========================
           5️⃣ Sauvegarde DB
           ========================= */
        MatchResult result = MatchResult.builder()
                .cvText(analysis.getCvText())
                .offerText(offer.getDescription())
                .score(score)
                .matchedJson(mapper.writeValueAsString(matchedCvSkills))
                .missingJson(mapper.writeValueAsString(missing))
                .createdAt(LocalDateTime.now())
                .candidat(candidat)
                .build();

        matchResultRepository.save(result);

        /* =========================
           6️⃣ Réponse API
           ========================= */
        MatchResponse response = new MatchResponse();
        response.setScore(score);
        response.setMatched(new ArrayList<>(matchedCvSkills));
        response.setMissing(new ArrayList<>(missing));

        return response;
    }

    /* =========================
       🔧 Logique de matching améliorée avec synonymes
       ========================= */
    private boolean isMatch(String cvSkill, String offerSkill) {
        // 1. Match exact
        if (cvSkill.equals(offerSkill)) {
            return true;
        }

        // 2. Synonymes et équivalences
        if (areSynonyms(cvSkill, offerSkill)) {
            return true;
        }

        // 3. Contenance (au moins 3 caractères)
        if (cvSkill.length() >= 3 && offerSkill.length() >= 3) {
            if (offerSkill.contains(cvSkill) || cvSkill.contains(offerSkill)) {
                return true;
            }
        }

        // 4. Similarité de mots (au moins 60% de mots en commun)
        String[] cvWords = cvSkill.split("\\s+");
        String[] offerWords = offerSkill.split("\\s+");

        Set<String> cvWordSet = new HashSet<>(Arrays.asList(cvWords));
        Set<String> offerWordSet = new HashSet<>(Arrays.asList(offerWords));

        Set<String> intersection = new HashSet<>(cvWordSet);
        intersection.retainAll(offerWordSet);

        int maxWords = Math.max(cvWordSet.size(), offerWordSet.size());
        if (maxWords > 0) {
            double similarity = (intersection.size() * 100.0) / maxWords;
            return similarity >= 60;
        }

        return false;
    }

    /* =========================
       🔧 Détection de synonymes
       ========================= */
    private boolean areSynonyms(String skill1, String skill2) {
        // Dictionnaire de synonymes : chaque groupe contient des termes équivalents
        List<Set<String>> synonymGroups = Arrays.asList(
            // REST API (avec pluriels)
            Set.of("rest api", "rest apis", "api rest", "apis rest", "rest", "restful api", 
                   "restful apis", "web services rest", "spring rest", "web service rest"),
            
            // Data Visualization
            Set.of("data visualization", "visualisation de donnees", "visualisation donnees", 
                   "conception dashboard", "tableaux de bord", "dashboard", "dashboards",
                   "tableau de bord"),
            
            // Databases
            Set.of("sql", "structured query language", "rdbms"),
            Set.of("postgres", "postgresql"),
            Set.of("mysql", "my sql"),
            
            // Machine Learning / AI
            Set.of("machine learning", "apprentissage automatique", "ml"),
            Set.of("artificial intelligence", "intelligence artificielle", "ia", "ai"),
            Set.of("deep learning", "apprentissage profond", "dl"),
            Set.of("ai training", "formation ia", "entrainement ia", "model training"),
            
            // Frontend
            Set.of("react", "reactjs", "react js"),
            Set.of("angular", "angularjs", "angular js"),
            Set.of("vue", "vuejs", "vue js"),
            
            // Backend
            Set.of("nodejs", "node js", "node"),
            Set.of("spring boot", "springboot"),
            Set.of("spring", "spring framework"),
            Set.of("django", "django framework"),
            
            // Architecture
            Set.of("microservices", "architecture microservices", "micro services",
                   "archiecture miseries"), // Typo du CV
            
            // Auth
            Set.of("jwt", "json web token", "authentication via jwt", "auth jwt"),
            
            // DevOps
            Set.of("ci cd", "cicd", "continuous integration", "integration continue"),
            Set.of("docker", "containerization", "conteneurisation"),
            Set.of("kubernetes", "k8s"),
            
            // Version Control
            Set.of("git", "github", "gitlab", "version control"),
            
            // Testing
            Set.of("unit testing", "tests unitaires", "junit", "testing"),
            
            // Other
            Set.of("erp", "enterprise resource planning", "erp seeing"), // Typo du CV
            Set.of("data analysis", "analyse de donnees", "data analytics")
        );

        // Vérifier si les deux skills appartiennent au même groupe de synonymes
        for (Set<String> group : synonymGroups) {
            if (group.contains(skill1) && group.contains(skill2)) {
                return true;
            }
        }

        return false;
    }

    /* =========================
       🔧 Normalisation texte
       ========================= */
    private String normalize(String s) {
        if (s == null) return "";

        String x = s.toLowerCase().trim();
        x = Normalizer.normalize(x, Normalizer.Form.NFD);
        x = x.replaceAll("\\p{M}+", "");     // accents
        x = x.replaceAll("[^a-z0-9 ]", " "); // symboles
        x = x.replaceAll("\\s+", " ");

        return x.trim();
    }

    /* =========================
       🔧 Réponse vide
       ========================= */
    private MatchResponse createEmptyResponse(
            CvAnalysis analysis,
            Offer offer,
            Candidat candidat,
            ObjectMapper mapper
    ) throws Exception {
        
        MatchResult result = MatchResult.builder()
                .cvText(analysis.getCvText())
                .offerText(offer.getDescription())
                .score(0)
                .matchedJson(mapper.writeValueAsString(new HashSet<>()))
                .missingJson(mapper.writeValueAsString(new HashSet<>()))
                .createdAt(LocalDateTime.now())
                .candidat(candidat)
                .build();

        matchResultRepository.save(result);

        MatchResponse response = new MatchResponse();
        response.setScore(0);
        response.setMatched(new ArrayList<>());
        response.setMissing(new ArrayList<>());

        return response;
    }
}
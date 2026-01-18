package ma.emsi.recrutementia.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.emsi.recrutementia.dto.MatchResponse;
import ma.emsi.recrutementia.dto.SkillsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String ollamaUrl;
    private final String model;

    public AiService(
            @Value("${app.ollama.url}") String ollamaUrl,
            @Value("${app.ollama.model:mistral}") String model
    ) {
        this.ollamaUrl = ollamaUrl;
        this.model = model;
    }

    public String corrigerTexte(String texteOcr) {
        String prompt = """
Tu es un correcteur OCR.
Corrige le texte (orthographe + mots cassés), sans inventer.
Retourne uniquement le texte corrigé, sans explications.

Texte OCR:
<<<
%s
>>>
""".formatted(texteOcr);

        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restClient.post()
                .uri(ollamaUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        return (String) resp.getOrDefault("response", "");
    }

    public SkillsResponse extractSkills(String text) throws Exception {
        String prompt = """
    Tu es un extracteur de compétences techniques à partir d'un texte.
    Retourne UNIQUEMENT un JSON valide EXACTEMENT au format:
    {"skills":["...","..."]}

    Règles STRICTES:
    - Extraire uniquement les compétences techniques : langages, frameworks, outils, technologies, méthodologies
    - Normaliser le nom (ex: "Spring boot" -> "Spring Boot", "postgres" -> "PostgreSQL", "react js" -> "React")
    - TOUJOURS répondre en FRANÇAIS (ex: "AI training" -> "Formation IA", "Data Analysis" -> "Analyse de données", "Machine Learning" -> "Apprentissage automatique")
    - Exclure les mots génériques : "développement", "conception", "expérience", "travail", "projet"
    - Pas d'explications, pas de markdown (```json), pas de texte en dehors du JSON
    - Si aucune compétence technique trouvée, retourner {"skills":[]}

    Exemples:
    Texte: "Développeur Java Spring Boot avec 3 ans d'expérience"
    Réponse: {"skills":["Java","Spring Boot"]}

    Texte: "AI model training and deep learning"
    Réponse: {"skills":["Formation de modèles IA","Apprentissage profond"]}

    Texte: "Power BI dashboards and data visualization"
    Réponse: {"skills":["Power BI","Visualisation de données"]}

    Texte à analyser:
    <<<
    %s
    >>>
    """.formatted(text);

        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restClient.post()
                .uri(ollamaUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        String json = (String) resp.getOrDefault("response", "{\"skills\":[]}");
        
        // Nettoyage de la réponse (au cas où Mistral ajoute des backticks)
        json = json.trim()
                   .replaceFirst("^```json\\s*", "")
                   .replaceFirst("\\s*```$", "")
                   .trim();
        
        return mapper.readValue(json, SkillsResponse.class);
    }

    public MatchResponse matchCvOffer(String cvText, String offerText) throws Exception {
        SkillsResponse cvSkills = extractSkills(cvText);
        SkillsResponse offerSkills = extractSkills(offerText);

        Set<String> cvSet = (cvSkills.getSkills() == null ? List.<String>of() : cvSkills.getSkills())
                .stream()
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        List<String> offerList = (offerSkills.getSkills() == null ? List.<String>of() : offerSkills.getSkills())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String s : offerList) {
            if (cvSet.contains(s.toLowerCase())) matched.add(s);
            else missing.add(s);
        }

        int score = offerList.isEmpty()
                ? 0
                : (int) Math.round(matched.size() * 100.0 / offerList.size());

        return new MatchResponse(score, matched, missing);
    }
}

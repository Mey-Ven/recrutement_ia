package ma.emsi.recrutementia.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.emsi.recrutementia.dto.SkillsResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AiService {

    private static final String OLLAMA_GENERATE_URL = "http://localhost:11434/api/generate";

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    // 1) Corriger texte OCR (retourne du texte)
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
                "model", "mistral",
                "prompt", prompt,
                "stream", false
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restClient.post()
                .uri(OLLAMA_GENERATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        return (String) resp.get("response"); // champ "response" dans la réponse Ollama 
    }

    // 2) Extraire skills (retourne JSON -> SkillsResponse)
    public SkillsResponse extractSkills(String cleanedCvText) throws Exception {

        String prompt = """
Tu es un extracteur de compétences à partir d'un CV.
Retourne UNIQUEMENT un JSON valide EXACTEMENT au format:
{"skills":["...","..."]}

Règles:
- skills = uniquement compétences/technologies/outils/frameworks/concepts
- normalise (ex: "Spring boot" -> "Spring Boot", "postgres" -> "PostgreSQL")
- pas d'explications, pas de markdown, pas de texte hors JSON

Texte CV:
<<<
%s
>>>
""".formatted(cleanedCvText);

        Map<String, Object> body = Map.of(
                "model", "mistral",
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restClient.post()
                .uri(OLLAMA_GENERATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        String json = (String) resp.get("response"); // JSON généré par le modèle 
        return mapper.readValue(json, SkillsResponse.class); // parse JSON -> objet 
    }
}
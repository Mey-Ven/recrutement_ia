package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.dto.SkillsResponse;
import ma.emsi.recrutementia.services.AiService;
import ma.emsi.recrutementia.services.OcrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final OcrService ocrService;
    private final AiService aiService;

    public CvController(OcrService ocrService, AiService aiService) {
        this.ocrService = ocrService;
        this.aiService = aiService;
    }

    // OCR only (cleaned text)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCv(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📄 Fichier reçu: " + file.getOriginalFilename());

            String text = ocrService.extractText(file);

            System.out.println("🧠 OCR OK");

            return ResponseEntity.ok(text);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur OCR: " + e.getMessage());
        }
    }

    // OCR -> skills
    @PostMapping("/upload-skills")
    public ResponseEntity<SkillsResponse> uploadCvAndExtractSkills(@RequestParam("file") MultipartFile file) {
        try {
            String cleaned = ocrService.extractText(file);
            SkillsResponse skills = aiService.extractSkills(cleaned);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

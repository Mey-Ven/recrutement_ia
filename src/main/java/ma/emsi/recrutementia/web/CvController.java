package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.services.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    @Autowired
    private OcrService ocrService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCv(@RequestParam("file") MultipartFile file) {
        try {
            String text = ocrService.extractText(file);
            return ResponseEntity.ok(text);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur OCR: " + e.getMessage());
        }
    }
}
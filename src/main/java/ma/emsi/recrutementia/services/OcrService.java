package ma.emsi.recrutementia.services;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrService {

    public String extractText(MultipartFile file) throws Exception {

        // Sauvegarde temporaire du fichier
        Path temp = Files.createTempFile("cv_", file.getOriginalFilename());
        Files.write(temp, file.getBytes());

        // Configuration de Tesseract
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // on va l’installer juste après
        tesseract.setLanguage("eng+fra");

        // Extraction du texte
        String result = tesseract.doOCR(temp.toFile());

        // Nettoyage
        Files.delete(temp);

        return result;
    }
}
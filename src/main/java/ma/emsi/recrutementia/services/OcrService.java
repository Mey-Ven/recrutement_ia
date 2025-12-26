package ma.emsi.recrutementia.services;

import ma.emsi.recrutementia.utils.OcrTextCleaner;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrService {

    private final String tesseractPath;
    private final String languages;

    public OcrService(
            @Value("${app.tesseract.path}") String tesseractPath,
            @Value("${app.tesseract.lang:eng+fra}") String languages
    ) {
        this.tesseractPath = tesseractPath;
        this.languages = languages;
    }

    public String extractText(MultipartFile file) throws Exception {
        Path temp = Files.createTempFile("cv_", "_" + safeName(file.getOriginalFilename()));
        Files.write(temp, file.getBytes());

        try {
            Tesseract tesseract = new Tesseract();
            // datapath must be the install folder that contains /tessdata
            tesseract.setDatapath(tesseractPath);
            tesseract.setLanguage(languages);

            String raw = tesseract.doOCR(temp.toFile());
            return OcrTextCleaner.clean(raw);

        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private String safeName(String name) {
        if (name == null) return "file";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

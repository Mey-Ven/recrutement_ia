package ma.emsi.recrutementia.services;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import ma.emsi.recrutementia.utils.OcrTextCleaner;

@Service
public class OcrService {

    public String extractText(MultipartFile file) throws Exception {

        Path temp = Files.createTempFile("cv_", "_" + safeName(file.getOriginalFilename()));
        Files.write(temp, file.getBytes());

        Tesseract tesseract = new Tesseract();

        // IMPORTANT: datapath = dossier d'installation (qui contient /tessdata)
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng+fra");

        String result = tesseract.doOCR(temp.toFile());
        result = OcrTextCleaner.clean(result);
        return result;
    }


    private String safeName(String name) {
        if (name == null) return "file";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
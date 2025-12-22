package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.services.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(
            value = "/corriger",
            consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE },
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String corriger(@RequestBody String texteOCR) {
        return aiService.corrigerTexte(texteOCR);
    }
}
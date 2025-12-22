package ma.emsi.recrutementia.web;

import ma.emsi.recrutementia.dto.SkillsResponse;
import ma.emsi.recrutementia.services.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class SkillsController {

    private final AiService aiService;

    public SkillsController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(
            value = "/skills",
            consumes = "text/plain",
            produces = "application/json"
    )
    public SkillsResponse extractSkills(@RequestBody String texteCorrige) throws Exception {
        return aiService.extractSkills(texteCorrige);
    }
}
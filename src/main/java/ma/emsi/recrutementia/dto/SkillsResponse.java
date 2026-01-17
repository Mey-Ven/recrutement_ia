package ma.emsi.recrutementia.dto;

import java.util.List;

public class SkillsResponse {
    private List<String> skills;

    public SkillsResponse() {}

    public SkillsResponse(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    
    
}

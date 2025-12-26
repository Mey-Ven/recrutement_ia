package ma.emsi.recrutementia.dto;

import java.util.List;

public class MatchResponse {
    private int score;
    private List<String> matched;
    private List<String> missing;

    public MatchResponse() {}

    public MatchResponse(int score, List<String> matched, List<String> missing) {
        this.score = score;
        this.matched = matched;
        this.missing = missing;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<String> getMatched() { return matched; }
    public void setMatched(List<String> matched) { this.matched = matched; }

    public List<String> getMissing() { return missing; }
    public void setMissing(List<String> missing) { this.missing = missing; }
}

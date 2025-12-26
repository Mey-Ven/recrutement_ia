package ma.emsi.recrutementia.dto;

public class MatchRequest {
    private String cvText;
    private String offerText;

    // Option 1: associer le match à un candidat
    private Long candidatId;

    public MatchRequest() {}

    public String getCvText() { return cvText; }
    public void setCvText(String cvText) { this.cvText = cvText; }

    public String getOfferText() { return offerText; }
    public void setOfferText(String offerText) { this.offerText = offerText; }

    public Long getCandidatId() { return candidatId; }
    public void setCandidatId(Long candidatId) { this.candidatId = candidatId; }
}
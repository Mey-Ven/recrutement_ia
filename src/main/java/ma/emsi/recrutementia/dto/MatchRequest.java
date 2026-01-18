package ma.emsi.recrutementia.dto;

public class MatchRequest {

    private Long cvAnalysisId;
    private Long offerId;
    private Long candidatId;

    public MatchRequest() {}

    public Long getCvAnalysisId() {
        return cvAnalysisId;
    }

    public void setCvAnalysisId(Long cvAnalysisId) {
        this.cvAnalysisId = cvAnalysisId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public Long getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(Long candidatId) {
        this.candidatId = candidatId;
    }
}

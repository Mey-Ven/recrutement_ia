package ma.emsi.recrutementia.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_result")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String cvText;

    @Column(columnDefinition = "TEXT")
    private String offerText;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String matchedJson;

    @Column(columnDefinition = "TEXT")
    private String missingJson;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;
}
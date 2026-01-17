package ma.emsi.recrutementia.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cv_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidat candidat;

    @Column(columnDefinition = "TEXT")
    private String cvText;

    @Column(columnDefinition = "TEXT")
    private String skillsJson;

    private LocalDateTime analyzedAt;
    
    
}

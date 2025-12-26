package ma.emsi.recrutementia.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchResult> matches = new ArrayList<>();
}
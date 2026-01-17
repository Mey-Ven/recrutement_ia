package ma.emsi.recrutementia.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import ma.emsi.recrutementia.dto.MatchResponse;
import ma.emsi.recrutementia.entities.*;
import ma.emsi.recrutementia.entities.MatchResult;
import ma.emsi.recrutementia.entities.Offer;
import ma.emsi.recrutementia.repositories.CvAnalysisRepository;
import ma.emsi.recrutementia.repositories.MatchResultRepository;
import ma.emsi.recrutementia.repositories.OfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchResultRepository matchResultRepository;
    private final ObjectMapper mapper;

    public MatchResponse matchCvWithOffer(
            CvAnalysis analysis,
            Offer offer,
            Candidat candidat
    ) throws Exception {

        // 1️⃣ Skills CV (DÉJÀ extraits)
        Set<String> cvSkills =
                mapper.readValue(analysis.getSkillsJson(), new TypeReference<Set<String>>() {});

        // 2️⃣ Skills Offre (simple parsing texte)
        Set<String> offerSkills =
                Arrays.stream(offer.getDescription().toLowerCase().split("[,\\n;]"))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toSet());

        // 3️⃣ Matching
        Set<String> matched = new HashSet<>(cvSkills);
        matched.retainAll(offerSkills);

        Set<String> missing = new HashSet<>(offerSkills);
        missing.removeAll(cvSkills);

        // 4️⃣ Score
        int score = offerSkills.isEmpty()
                ? 0
                : (int) ((matched.size() * 100.0) / offerSkills.size());

        // 5️⃣ Sauvegarde
        MatchResult result = MatchResult.builder()
                .cvText(analysis.getCvText())
                .offerText(offer.getDescription())
                .score(score)
                .matchedJson(mapper.writeValueAsString(matched))
                .missingJson(mapper.writeValueAsString(missing))
                .createdAt(LocalDateTime.now())
                .candidat(candidat)
                .build();

        matchResultRepository.save(result);

        // 6️⃣ Réponse API
        MatchResponse response = new MatchResponse();
        response.setScore(score);
        response.setMatched(new ArrayList<>(matched));
        response.setMissing(new ArrayList<>(missing));
        return response;
    }
}
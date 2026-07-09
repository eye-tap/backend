package ch.ethz.eyetap.controller;


import ch.ethz.eyetap.dto.CreateSurveyDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import ch.ethz.eyetap.repository.ReadingSessionRepository;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.repository.UserRepository;
import ch.ethz.eyetap.service.AnnotationSessionService;
import ch.ethz.eyetap.service.AuthService;
import ch.ethz.eyetap.service.SurveyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/generator")
@RequiredArgsConstructor
public class MagicLinkGeneratorController {
    private final SurveyService surveyService;
    private final AuthService authService;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final AnnotationSessionService annotationSessionService;
    private final AnnotationSessionRepository annotationSessionRepository;

    // todo: this is insecure as of now, fix later
    @Transactional
    @PostMapping("/draw/{surveyId}")
    public Map.Entry<String, String> draw(
            @RequestBody Set<Long> readingSessionIds,
            @PathVariable Long surveyId) {
        Survey survey = this.surveyService.getById(surveyId);
        List<String> pseudonyms = this.surveyService.generateFreshPseudonyms(1);
        List<AuthService.SurveyParticipant> surveyParticipantsBatch = this.authService.createSurveyParticipantsBatch(pseudonyms);
        AuthService.SurveyParticipant participant = surveyParticipantsBatch.getFirst();

        survey.getUsers().add(participant.user());
        participant.user().setSurveys(new HashSet<>(List.of(survey)));

        User user = this.userRepository.save(participant.user());
        survey = this.surveyRepository.save(survey);

        Set<AnnotationSession> annotationSessions = new HashSet<>();

        for (Long readingSessionId : readingSessionIds) {
            ReadingSession readingSession = this.readingSessionRepository.findById(readingSessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading session with id " + readingSessionId + " not found."));
            AnnotationSession initialize = this.annotationSessionService.initialize(survey, user, readingSession);
            annotationSessions.add(initialize);
        }

        survey.getAnnotationSessions().addAll(annotationSessions);
        this.annotationSessionRepository.saveAll(annotationSessions);
        this.surveyRepository.save(survey);

        return new AbstractMap.SimpleEntry<>(
                participant.user().getUsername(),
                participant.password()
        );
    }

}

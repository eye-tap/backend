package ch.ethz.eyetap.service;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.CreateSurveyDto;
import ch.ethz.eyetap.dto.SurveyCreatedDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.ReadingSessionRepository;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final AuthService authService;
    private final PseudonymeGeneratorService pseudonymeGeneratorService;
    private final UserRepository userRepository;
    private final AnnotationSessionService annotationSessionService;
    private final ReadingSessionRepository readingSessionRepository;
    private final EntityMapper entityMapper;

    public SurveyCreatedDto create(User admin, CreateSurveyDto createSurveyDto) {

        Map<String, String> surveyUsers = new HashMap<>();
        Set<User> userSet = new HashSet<>();
        for (int i = 0; i < createSurveyDto.users().intValue(); i++) {
            String pseudonym;
            do {
                pseudonym = this.pseudonymeGeneratorService.generatePseudonym();
            } while (this.userRepository.existsByUsername(pseudonym));
            AuthService.SurveyParticipant participant = this.authService.createSurveyParticipant(pseudonym);
            surveyUsers.put(pseudonym, participant.password());
            userSet.add(participant.user());
        }

        Survey survey = new Survey();
        survey.setUsers(userSet);
        survey.setTitle(createSurveyDto.title());
        survey.setDescription(createSurveyDto.description());
        survey.setAdmin(Set.of(admin));
        survey = this.surveyRepository.save(survey);

        Set<AnnotationSession> annotationSessions = new HashSet<>();
        for (Long readingSessionId : createSurveyDto.readingSessionIds()) {
            ReadingSession readingSession = this.readingSessionRepository.getReferenceById(readingSessionId);
            for (User user : userSet) {
                AnnotationSession annotationSession = this.annotationSessionService.create(survey, user, readingSession);
                annotationSessions.add(annotationSession);
            }
        }

        survey.setAnnotationSessions(annotationSessions);
        survey = this.surveyRepository.save(survey);

        return new SurveyCreatedDto(
                this.entityMapper.toSurveyDto(survey),
                surveyUsers);
    }

    public Survey getById(Long id) {
        return this.surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No servey was found with id: " + id));
    }

    public Set<Survey> getAll() {
        return new HashSet<>(this.surveyRepository.findAll());
    }


    public void delete(Long id) {
        Survey survey = this.getById(id);
        for (AnnotationSession annotationSession : survey.getAnnotationSessions()) {
            this.annotationSessionService.delete(annotationSession);
        }
        this.surveyRepository.deleteById(id);
    }
}

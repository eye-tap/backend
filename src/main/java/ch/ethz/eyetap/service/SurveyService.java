package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final AuthService authService;
    private final PseudonymeGeneratorService pseudonymeGeneratorService;
    private final UserRepository userRepository;
    private final AnnotationSessionService annotationSessionService;
    private final ReadingSessionRepository readingSessionRepository;

    @Transactional
    @CacheEvict(value = "surveys_all", allEntries = true)
    public SurveyCreatedDto create(User admin, CreateSurveyDto createSurveyDto) {
        log.info("Creating survey");
        long startTime = System.nanoTime();
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
        log.info("Created survey users");
        Survey survey = new Survey();
        survey.setUsers(userSet);
        survey.setTitle(createSurveyDto.title());
        survey.setDescription(createSurveyDto.description());
        survey.setAdmin(Set.of(admin));
        survey = this.surveyRepository.save(survey);

        log.info("Creating annotation sessions");
        Set<AnnotationSession> annotationSessions = new HashSet<>();
        for (Long readingSessionId : createSurveyDto.readingSessionIds()) {
            ReadingSession readingSession = this.readingSessionRepository.findWithFixations(readingSessionId);
            readingSession.getText().getCharacterBoundingBoxes().size(); // just for loading the object
            for (User user : userSet) {
                AnnotationSession annotationSession = this.annotationSessionService.create(survey, user, readingSession);
                annotationSessions.add(annotationSession);
            }
        }

        survey.setAnnotationSessions(annotationSessions);
        Long id = this.surveyRepository.save(survey).getId();
        long intermediate = System.nanoTime();
        log.info("Survey created! ");
        SurveyCreatedDto surveyCreatedDto = mapFromSurvey(id, surveyUsers);
        long endTime = System.nanoTime();

        log.info("Survey created in {} ms, of which {} ms were spend on db operation and {} ms on json object creation",
                (endTime - startTime) / 1000000,
                (intermediate - startTime) / 1000000,
                (endTime - intermediate) / 1000000);


        return surveyCreatedDto;
    }

    private SurveyCreatedDto mapFromSurvey(Long id, Map<String, String> surveyUsers) {
        SurveyDto surveyDto = mapToSurveyDto(id);
        return new SurveyCreatedDto(
                surveyDto
                , surveyUsers
        );
    }

    @SneakyThrows
    public SurveyDto mapToSurveyDto(Long surveyId) {
        // Run the native query
        Object result = this.surveyRepository.findSurveyWithSessionsNative(surveyId);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found with id " + surveyId);
        }

        Object[] row = (Object[]) result;

        Long id = ((Number) row[0]).longValue();
        String title = (String) row[1];
        String description = (String) row[2];

        // Parse arrays
        Set<Long> userIds = parseLongArray(row[3]);
        Set<Long> annotationSessionIds = parseLongArray(row[4]);
        Object[] annotatorIdsArr = row[5] != null ? (Object[]) row[5] : null;
        Object[] readingSessionIdsArr = row[6] != null ? (Object[]) row[6] : null;
        Object[] readingSessionReaderIdsArr = row[7] != null ? (Object[]) row[7] : null;
        Object[] readingSessionTextIdsArr = row[8] != null ? (Object[]) row[8] : null;
        Object[] readingSessionTextTitlesArr = row[9] != null ? (Object[]) row[9] : null;

        // annotation counts come as Numbers, not arrays
        Number totalAnnotations = (Number) row[10];
        Number totalAnnotated = (Number) row[11];

        Object[] lastEditedArr = row[12] != null ? (Object[]) row[12] : null;
        Object[] uploadedAtArr = row[13] != null ? (Object[]) row[12] : null;

        // Build ShallowAnnotationSessionDto
        Set<ShallowAnnotationSessionDto> sessions = new LinkedHashSet<>();
        if (annotationSessionIds != null && !annotationSessionIds.isEmpty()) {
            int n = annotationSessionIds.size();
            Long[] sessionIds = annotationSessionIds.toArray(new Long[0]);

            for (int i = 0; i < n; i++) {

                LocalDateTime lastEdited =
                        lastEditedArr != null && i < lastEditedArr.length
                                ? (LocalDateTime) lastEditedArr[i]
                                : null;

                LocalDateTime uploadedAt =
                        lastEditedArr != null && i < lastEditedArr.length
                                ? (LocalDateTime) uploadedAtArr[i]
                                : null;


                ShallowAnnotationSessionDto session = new ShallowAnnotationSessionDto(
                        sessionIds[i],
                        annotatorIdsArr != null && i < annotatorIdsArr.length ? ((Number) annotatorIdsArr[i]).longValue() : null,
                        new AnnotationsMetaDataDto(
                                totalAnnotations != null ? totalAnnotations.intValue() : 0,
                                totalAnnotated != null ? totalAnnotated.intValue() : 0
                        ),
                        new ShallowReadingSessionDto(
                                readingSessionIdsArr != null && i < readingSessionIdsArr.length ? ((Number) readingSessionIdsArr[i]).longValue() : null,
                                readingSessionReaderIdsArr != null && i < readingSessionReaderIdsArr.length ? ((Number) readingSessionReaderIdsArr[i]).longValue() : null,
                                readingSessionTextIdsArr != null && i < readingSessionTextIdsArr.length ? ((Number) readingSessionTextIdsArr[i]).longValue() : null,
                                readingSessionTextTitlesArr != null && i < readingSessionTextTitlesArr.length ? (String) readingSessionTextTitlesArr[i] : null,
                                uploadedAt
                        ),
                        lastEdited
                );
                sessions.add(session);
            }
        }

        return new SurveyDto(
                id,
                userIds,
                title,
                description,
                sessions
        );
    }

    // Helper: parse Object[] returned from ARRAY_AGG in Postgres
    Set<Long> parseLongArray(Object arrObj) {
        Set<Long> out = new LinkedHashSet<>();
        if (arrObj != null) {
            Object[] array = (Object[]) arrObj;
            for (Object o : array) {
                if (o != null) out.add(((Number) o).longValue());
            }
        }
        return out;
    }

    public Survey getById(Long id) {
        return this.surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No servey was found with id: " + id));
    }

    @Cacheable("surveys_all")
    public Set<SurveyDto> getAll() {
        return this.surveyRepository.findAll()
                .stream()
                .map(survey -> this.mapToSurveyDto(survey.getId()))
                .collect(Collectors.toSet());
    }

    @CacheEvict(value = "surveys_all", allEntries = true)
    public void delete(Long id) {
        Survey survey = this.getById(id);
        for (AnnotationSession annotationSession : survey.getAnnotationSessions()) {
            this.annotationSessionService.delete(annotationSession);
        }
        this.surveyRepository.deleteById(id);
    }
}

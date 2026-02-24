package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.MachineAnnotation;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.annotation.Text;
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
    private final MachineAnnotationRepository machineAnnotationRepository;
    private final AnnotationSessionRepository annotationSessionRepository;
    private final FixationRepository fixationRepository;

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
            for (String annotationTitle : this.machineAnnotationRepository.findAllMachineAnnotationTitle(readingSessionId)) {
                Set<MachineAnnotation> preAnnotations = this.machineAnnotationRepository.findByTitleAndReadingSession(annotationTitle, readingSessionId);
                for (User user : userSet) {
                    AnnotationSession annotationSession = this.annotationSessionService.create(survey, user, readingSession);
                    annotationSessions.add(annotationSession);
                    annotationSession.setMachineAnnotations(preAnnotations);
                    annotationSession.setDescription(readingSession.getText().getTitle() + ", " + readingSession.getReader().getId() + ", " + annotationTitle);
                    for (MachineAnnotation preAnnotation : preAnnotations) {
                        preAnnotation.getAnnotationSessions().add(annotationSession);
                    }
                }
                this.machineAnnotationRepository.saveAll(preAnnotations);
            }

            for (User user : userSet) {
                AnnotationSession annotationSession = this.annotationSessionService.create(survey, user, readingSession);
                annotationSessions.add(annotationSession);
                annotationSession.setDescription(readingSession.getText().getTitle() + ", " + readingSession.getReader().getId() + ", NO PRE-ANNOTATION");

            }
        }

        survey.setAnnotationSessions(annotationSessions);
        this.annotationSessionRepository.saveAll(annotationSessions);
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
        // Load survey + users
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found with id " + surveyId));

        Set<Long> userIds = survey.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<AnnotationSession> sessions = annotationSessionRepository.findBySurveyIdWithAnnotatorAndReadingSession(surveyId);

        if (sessions.isEmpty()) {
            return new SurveyDto(
                    survey.getId(),
                    userIds,
                    survey.getTitle(),
                    survey.getDescription(),
                    Collections.emptySet()
            );
        }

        List<Long> sessionIds = sessions.stream().map(AnnotationSession::getId).toList();
        List<Long> readingSessionIds = sessions.stream()
                .map(s -> s.getReadingSession().getId())
                .toList();

        List<Object[]> fixationCountList = fixationRepository.findFixationCounts(readingSessionIds);
        Map<Long, Integer> fixationCounts = fixationCountList.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> ((Number) r[1]).intValue()));

        List<Object[]> annotatedCountList = annotationSessionRepository.findAnnotatedCounts(sessionIds);
        Map<Long, Integer> annotatedCounts = annotatedCountList.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> ((Number) r[1]).intValue()));

        Set<ShallowAnnotationSessionDto> sessionDtos = new LinkedHashSet<>();

        for (AnnotationSession a : sessions) {
            ReadingSession rs = a.getReadingSession();
            Long readingSessionId = rs.getId();
            Long readingSessionReaderId = rs.getReader().getId();
            Text t = rs.getText();
            Long textId = t.getId();
            String textTitle = t.getTitle();

            int fixationCount = fixationCounts.getOrDefault(readingSessionId, 0);
            int annotatedCount = annotatedCounts.getOrDefault(a.getId(), 0);

            ShallowReadingSessionDto readingSessionDto = new ShallowReadingSessionDto(
                    readingSessionId,
                    readingSessionReaderId,
                    textId,
                    textTitle,
                    rs.getUploadedAt()
            );

            AnnotationsMetaDataDto metaData = new AnnotationsMetaDataDto(
                    fixationCount,
                    annotatedCount
            );

            ShallowAnnotationSessionDto sessionDto = new ShallowAnnotationSessionDto(
                    a.getId(),
                    a.getAnnotator().getUser().getId(),
                    metaData,
                    readingSessionDto,
                    a.getLastEdited(),
                    a.getDescription()
            );

            sessionDtos.add(sessionDto);
        }

        return new SurveyDto(
                survey.getId(),
                userIds,
                survey.getTitle(),
                survey.getDescription(),
                sessionDtos
        );
    }

    public Survey getById(Long id) {
        return this.surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No servey was found with id: " + id));
    }

    @Cacheable("surveys_all")
    public Set<SurveyDto> getAll(Long userId) {
        return this.surveyRepository.findAll()
                .stream()
                .filter(survey -> hasAccessToSurvey(userId, survey))
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

    public boolean hasAccessToSurvey(Long userId, Survey survey){
        return survey.getAdmin()
                .stream()
                .map(User::getId)
                .anyMatch(adminId -> Objects.equals(adminId, userId));
    }
}

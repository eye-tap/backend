package ch.ethz.eyetap.service;


import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import ch.ethz.eyetap.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AnnotationSessionService {

    private final ReadingSessionService readingSessionService;
    private final AnnotationSessionRepository sessionRepository;
    private final AnnotationSessionRepository annotationSessionRepository;
    private final EntityMapper entityMapper;
    private final UserRepository userRepository;


    public Set<Long> annotationSessionIdsByUserId(Annotator annotator) {
        return this.annotationSessionRepository.findAllIdsByAnnotator(annotator);
    }

    public UserSurveyProgressDto getAnnotationSessionsUserId(Long userId, Annotator annotator) {
        Set<Long> sessions = this.annotationSessionRepository.findAllIdsByAnnotator(annotator);
        long total = sessions.size();
        long done = 0;
        for (Long sessionId : sessions) {
            if (
                    this.sessionRepository.countSetAnnotationsByAnnotationSessionId(sessionId)
                            == this.sessionRepository.countTotalFixationsByAnnotationSessionId(sessionId)
            ) {
                done++;
            }
        }
        return new UserSurveyProgressDto(userId, total, done);
    }

    public AnnotationsMetaDataDto calculateAnnotationsMetaData(Long annotationSessionId) {
        int total = Math.toIntExact(this.sessionRepository.countTotalFixationsByAnnotationSessionId(annotationSessionId));
        int set = Math.toIntExact(this.sessionRepository.countSetAnnotationsByAnnotationSessionId(annotationSessionId));

        return new AnnotationsMetaDataDto(total, set);
    }

    public ShallowAnnotationSessionDto calculateShallowAnnotationSessionDto(Long annotationSessionId) {
        return new ShallowAnnotationSessionDto(annotationSessionId,
                this.annotationSessionRepository.annotatorByAnnotationSessionId(annotationSessionId),
                this.calculateAnnotationsMetaData(annotationSessionId),
                this.readingSessionService.shallowReadingSessionDto(
                        this.sessionRepository.readingSessionByAnnotationSessionId(annotationSessionId)),
                this.annotationSessionRepository.lastEditedByAnnotationSessionId(annotationSessionId),
                this.annotationSessionRepository.descriptionByAnnotationSessionId(annotationSessionId)
        );
    }

    public AnnotationSession create(Survey survey, User user, ReadingSession readingSession) {
        log.info("Creating annotation sessions for reading session {}", readingSession.getId());
        AnnotationSession annotationSession = new AnnotationSession();
        annotationSession.setAnnotator(user.getAnnotator());
        annotationSession.setReadingSession(readingSession);
        annotationSession.setSurvey(survey);
        annotationSession.setLastEdited(LocalDateTime.now());

        return this.annotationSessionRepository.save(annotationSession);
    }

    public void delete(AnnotationSession annotationSession) {
        this.annotationSessionRepository.delete(annotationSession);
    }

    public AnnotationSessionDto calculateAnnotationSessionDtoById(final Long id) {
        AnnotationSession referenceById = this.annotationSessionRepository.getReferenceById(id);
        return this.calculateAnnotationSessionDto(referenceById);
    }

    public AnnotationSessionDto calculateAnnotationSessionDto(final AnnotationSession annotationSession) {
        return annotationSessionDto(annotationSession, this.calculateAnnotationsMetaData(annotationSession.getId()));
    }

    private AnnotationSessionDto annotationSessionDto(final AnnotationSession annotationSession, AnnotationsMetaDataDto metaDataDto) {
        Set<AnnotationDto> annotations = new HashSet<>();
        for (UserAnnotation userAnnotation : annotationSession.getUserAnnotations()) {
            AnnotationDto annotationDto = new AnnotationDto(userAnnotation.getId(),
                    AnnotationType.ANNOTATED,
                    new FixationDto(userAnnotation.getFixation().getId(),
                            userAnnotation.getFixation().getX(),
                            userAnnotation.getFixation().getY(),
                            null),
                    this.entityMapper.toBoundingBoxDto(userAnnotation.getCharacterBoundingBox()),
                    null,
                    null);
            annotations.add(annotationDto);
        }

        for (MachineAnnotation machineAnnotation : annotationSession.getMachineAnnotations()) {
            AnnotationDto annotationDto = new AnnotationDto(machineAnnotation.getId(),
                    AnnotationType.MACHINE_ANNOTATED,
                    new FixationDto(machineAnnotation.getFixation().getId(),
                            machineAnnotation.getFixation().getX(),
                            machineAnnotation.getFixation().getY(),
                            machineAnnotation.getFixation().getDisagreement()),
                    this.entityMapper.toBoundingBoxDto(machineAnnotation.getCharacterBoundingBox()),
                    machineAnnotation.getDGeomWeight(),
                    machineAnnotation.getPShareWeight());
            annotations.add(annotationDto);
        }

        return new AnnotationSessionDto(annotationSession.getId(),
                annotationSession.getAnnotator().getId(),
                annotations,
                metaDataDto,
                this.readingSessionService.createReadingSessionDto(annotationSession.getReadingSession()),
                annotationSession.getLastEdited());
    }
}

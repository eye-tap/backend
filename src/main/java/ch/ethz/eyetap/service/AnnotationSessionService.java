package ch.ethz.eyetap.service;


import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.dto.AnnotationsMetaDataDto;
import ch.ethz.eyetap.dto.ShallowAnnotationSessionDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.Annotator;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public Set<Long> annotationSessionIdsByUserId(Annotator annotator) {
        return this.annotationSessionRepository.findAllIdsByAnnotator(annotator);
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
                        this.sessionRepository.readingSessionByAnnotationSessionId(annotationSessionId))
        );
    }

    public AnnotationSession create(Survey survey, User user, ReadingSession readingSession) {
        log.info("Creating annotation sessions for reading session {}", readingSession.getId());
        // TODO: 13.02.2026 use pre annotations
        AnnotationSession annotationSession = new AnnotationSession();
        annotationSession.setAnnotator(user.getAnnotator());
        annotationSession.setReadingSession(readingSession);
        annotationSession.setSurvey(survey);

        return this.annotationSessionRepository.save(annotationSession);
    }

    public void delete(AnnotationSession annotationSession) {
        this.annotationSessionRepository.delete(annotationSession);
    }

    public AnnotationSessionDto calculateAnnotationSessionDtoById(final Long id) {
        AnnotationSession referenceById = this.annotationSessionRepository.getReferenceById(id);
        log.info("Calculating annotation session DTO for reading session {}", referenceById);
        return this.calculateAnnotationSessionDto(referenceById);
    }

    public AnnotationSessionDto calculateAnnotationSessionDto(final AnnotationSession annotationSession) {
        return this.entityMapper.toAnnotationSessionDto(annotationSession,
                this.calculateAnnotationsMetaData(annotationSession.getId()));
    }
}

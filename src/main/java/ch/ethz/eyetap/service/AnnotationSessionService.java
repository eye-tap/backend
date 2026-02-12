package ch.ethz.eyetap.service;


import ch.ethz.eyetap.dto.AnnotationsMetaDataDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotationRepository;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AnnotationSessionService {

    private final AnnotationRepository annotationRepository;
    private final AnnotationSessionRepository annotationSessionRepository;

    public Set<AnnotationSession> getAnnotationSessionsByUser(User user) {
        return this.annotationSessionRepository.findAllByAnnotator(user.getAnnotator());
    }

    public AnnotationsMetaDataDto calculateAnnotationsMetaData(AnnotationSession session) {
        return new AnnotationsMetaDataDto(0, 0);
        // todo: collect actual data here
    }

    public AnnotationSession create(Survey survey, User user, ReadingSession readingSession) {
        log.info("Creating annotation sessions for reading session {}", readingSession.getId());
        AnnotationSession annotationSession = new AnnotationSession();
        annotationSession.setAnnotator(user.getAnnotator());
        annotationSession.setReadingSession(readingSession);
        annotationSession.setSurvey(survey);

        annotationSession = this.annotationSessionRepository.save(annotationSession);

        Set<Annotation> annotations = new HashSet<>();

        Set<Fixation> unannotatedFixations = new HashSet<>();
        for (Fixation fixation : readingSession.getFixations()) {
            boolean success = createUnsavedAnnotationIfAlignsWithCharacterBoundingBox(readingSession, fixation, annotationSession, annotations);
            if (!success) {
                unannotatedFixations.add(fixation);
            }
        }

        for (Fixation unannotatedFixation : unannotatedFixations) {
            Annotation annotation = new Annotation();
            annotation.setFixation(unannotatedFixation);
            annotation.setAnnotationType(AnnotationType.UNANNOTATED);
            annotation.setAnnotationSession(annotationSession);
            annotations.add(annotation);
        }

        this.annotationRepository.saveAll(annotations);

        annotationSession.setAnnotations(annotations);

        return this.annotationSessionRepository.save(annotationSession);
    }

    private boolean createUnsavedAnnotationIfAlignsWithCharacterBoundingBox(ReadingSession readingSession, Fixation fixation, AnnotationSession annotationSession, Set<Annotation> annotations) {
        for (CharacterBoundingBox characterBoundingBox : readingSession.getText().getCharacterBoundingBoxes()) {
            if (!characterBoundingBox.getBoundingBox().contains(fixation.getX(), fixation.getY())) continue;
            Annotation annotation = new Annotation();
            annotation.setFixation(fixation);
            annotation.setAnnotationType(AnnotationType.MACHINE_ANNOTATED);
            annotation.setCharacterBoundingBox(characterBoundingBox);
            annotation.setAnnotationSession(annotationSession);
            annotations.add(annotation);
            return true;
        }
        return false;
    }

    public void delete(AnnotationSession annotationSession) {
        this.annotationSessionRepository.delete(annotationSession);
    }
}

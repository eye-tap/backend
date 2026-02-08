package ch.ethz.eyetap.service;


import ch.ethz.eyetap.dto.AnnotationsMetaDataDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotationRepository;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnnotationSessionService {

    private final AnnotationRepository annotationRepository;
    private final AnnotationSessionRepository annotationSessionRepository;

    public Set<AnnotationSession> getAnnotationSessionsByUser(User user) {
        return user.getAnnotator().getAnnotationSessions();
    }

    public AnnotationsMetaDataDto calculateAnnotationsMetaData(AnnotationSession session) {
        int total = session.getAnnotations().size();
        int set = (int) session.getAnnotations().stream().filter(annotation ->
                        annotation.getAnnotationType().equals(AnnotationType.ANNOTATED)
                                || annotation.getAnnotationType().equals(AnnotationType.MACHINE_ANNOTATED))
                .count();

        return new AnnotationsMetaDataDto(total, set);
    }

    public AnnotationSession create(Survey survey, User user, ReadingSession readingSession) {
        AnnotationSession annotationSession = new AnnotationSession();
        annotationSession.setAnnotator(user.getAnnotator());
        annotationSession.setReadingSession(readingSession);
        annotationSession.setSurvey(survey);

        annotationSession = this.annotationSessionRepository.save(annotationSession);

        Set<Annotation> annotations = new HashSet<>();

        Set<Fixation> unannotatedFixations = new HashSet<>();
        for (Fixation fixation : readingSession.getFixations()) {
            boolean success = createAnnotationIfAlignsWithCharacterBoundingBox(readingSession, fixation, annotationSession, annotations);
            if (!success) {
                unannotatedFixations.add(fixation);
            }
        }

        for (Fixation unannotatedFixation : unannotatedFixations) {
            Annotation annotation = new Annotation();
            annotation.setFixation(unannotatedFixation);
            annotation.setAnnotationType(AnnotationType.UNANNOTATED);
            annotation.setAnnotationSession(annotationSession);
            annotation = this.annotationRepository.save(annotation);
            annotations.add(annotation);
        }

        annotationSession.setAnnotations(annotations);

        return this.annotationSessionRepository.save(annotationSession);
    }

    private boolean createAnnotationIfAlignsWithCharacterBoundingBox(ReadingSession readingSession, Fixation fixation, AnnotationSession annotationSession, Set<Annotation> annotations) {
        for (CharacterBoundingBox characterBoundingBox : readingSession.getText().getCharacterBoundingBoxes()) {
            if (!characterBoundingBox.getBoundingBox().contains(fixation.getX(), fixation.getY())) continue;
            Annotation annotation = new Annotation();
            annotation.setFixation(fixation);
            annotation.setAnnotationType(AnnotationType.MACHINE_ANNOTATED);
            annotation.setCharacterBoundingBox(characterBoundingBox);
            annotation.setAnnotationSession(annotationSession);
            annotation = this.annotationRepository.save(annotation);
            annotations.add(annotation);
            return true;
        }
        return false;
    }

    public void delete(AnnotationSession annotationSession) {
        this.annotationSessionRepository.delete(annotationSession);
    }
}

package ch.ethz.eyetap.service;

import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.repository.AnnotationRepository;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnnotationService {

    private final AnnotationSessionRepository annotationSessionRepository;
    private final AnnotationRepository annotationRepository;

    public AnnotationSession annotateMany(AnnotationSession annotationSession, Map<Fixation, CharacterBoundingBox> annotations) {
        AnnotationSession session = annotationSession;
        for (Map.Entry<Fixation, CharacterBoundingBox> fixationCharacterBoundingBoxEntry : annotations.entrySet()) {
            session = annotate(session, fixationCharacterBoundingBoxEntry.getKey(), fixationCharacterBoundingBoxEntry.getValue());
        }
        return session;
    }

    public AnnotationSession annotate(AnnotationSession annotationSession, Fixation fixation, CharacterBoundingBox characterBoundingBox) {
        var currentAnnotation = annotationSession.getAnnotations().stream().filter(annotation -> annotation.getFixation().equals(fixation)).findFirst()
                .orElseGet(Annotation::new);

        currentAnnotation.setFixation(fixation);
        currentAnnotation.setCharacterBoundingBox(characterBoundingBox);
        currentAnnotation.setAnnotationSession(annotationSession);
        currentAnnotation.setAnnotationType(AnnotationType.USER);

        annotationRepository.save(currentAnnotation);

        annotationSession.getAnnotations().add(currentAnnotation);
        return annotationSessionRepository.save(annotationSession);
    }


}

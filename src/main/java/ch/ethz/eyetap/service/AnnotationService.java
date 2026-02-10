package ch.ethz.eyetap.service;

import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.repository.AnnotationRepository;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import ch.ethz.eyetap.repository.CharacterBoundingBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnotationService {


    private final AnnotationSessionRepository annotationSessionRepository;
    private final AnnotationRepository annotationRepository;
    private final CharacterBoundingBoxRepository characterBoundingBoxRepository;

    public AnnotationSession annotateMany(AnnotationSession annotationSession, Map<Annotation, CharacterBoundingBox> annotations) {
        AnnotationSession session = annotationSession;
        for (Map.Entry<Annotation, CharacterBoundingBox> fixationCharacterBoundingBoxEntry : annotations.entrySet()) {
            session = annotate(session, fixationCharacterBoundingBoxEntry.getKey(), fixationCharacterBoundingBoxEntry.getValue());
        }
        return session;
    }

    public AnnotationSession annotate(AnnotationSession annotationSession, Annotation annotation, CharacterBoundingBox characterBoundingBox) {

        annotation.setCharacterBoundingBox(characterBoundingBox);
        annotation.setAnnotationSession(annotationSession);
        annotation.setAnnotationType(AnnotationType.ANNOTATED);

        annotationRepository.save(annotation);

        return annotationSessionRepository.save(annotationSession);
    }


    public AnnotationSession annotate(AnnotationSession session, Map<Long, Long> annotations) {
        Map<Annotation, CharacterBoundingBox> map = annotations.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> {
                            Long annotationId = entry.getKey();
                            return this.findAnnotationByIdInSession(session, annotationId);
                        },
                        entry -> {
                            Long characterBoundingBoxId = entry.getValue();
                            return this.characterBoundingBoxRepository.findById(characterBoundingBoxId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Character bounding box id " + characterBoundingBoxId + " not found"
                                    ));
                        }
                ));

        return this.annotateMany(session, map);

    }

    private Annotation findAnnotationByIdInSession(AnnotationSession annotationSession, Long annotationId) {
        return annotationSession.getAnnotations().stream().filter(annotation -> annotation.getId().equals(annotationId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annotation id " + annotationId + " not found"));
    }
}

package ch.ethz.eyetap.service;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.model.annotation.Annotation;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.AnnotationType;
import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import ch.ethz.eyetap.repository.AnnotationRepository;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import ch.ethz.eyetap.repository.CharacterBoundingBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnotationService {


    private final AnnotationSessionRepository annotationSessionRepository;
    private final AnnotationRepository annotationRepository;
    private final CharacterBoundingBoxRepository characterBoundingBoxRepository;
    private final AnnotationSessionService annotationSessionService;

    public AnnotationSession annotateMany(AnnotationSession annotationSession, Map<Annotation, CharacterBoundingBox> annotations) {
        // Update each annotation in memory
        annotations.forEach((annotation, bbox) -> {
            annotation.setCharacterBoundingBox(bbox);
            annotation.setAnnotationSession(annotationSession);
            annotation.setAnnotationType(AnnotationType.ANNOTATED);
        });

        // Bulk save all annotations at once
        annotationRepository.saveAll(annotations.keySet());

        // Update the annotation session
        annotationSession.getAnnotations().addAll(annotations.keySet());
        annotationSession.setLastEdited(LocalDateTime.now());
        return annotationSessionRepository.save(annotationSession);
    }


    public AnnotationSessionDto annotate(Long sessionId, Map<Long, Long> annotations) {

        AnnotationSession session = this.annotationSessionRepository.getReferenceById(sessionId);

        Map<Annotation, CharacterBoundingBox> map = annotations.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> {
                            Long annotationId = entry.getKey();
                            return this.findAnnotationByIdInSession(sessionId, annotationId);
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

        AnnotationSession annotationSession = this.annotateMany(session, map);

        return this.annotationSessionService.calculateAnnotationSessionDto(annotationSession);
    }

    private Annotation findAnnotationByIdInSession(Long sessionId, Long annotationId) {
        return this.annotationRepository.findByIdAndSessionId(sessionId, annotationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annotation id " + annotationId + " not found"));
    }
}

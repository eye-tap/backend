package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link AnnotationSession}
 */
public record ShallowAnnotationSessionDto(
        Long id,
        Long annotator,
        AnnotationsMetaDataDto annotationsMetaData,
        ShallowReadingSessionDto readingSession,
        LocalDateTime lastEdited,
        String description) {
}
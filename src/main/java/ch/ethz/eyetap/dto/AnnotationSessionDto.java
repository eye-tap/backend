package ch.ethz.eyetap.dto;

import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link ch.ethz.eyetap.model.AnnotationSession}
 */
@Value
public class AnnotationSessionDto implements Serializable {
    Long annotator;
    Set<AnnotationDto> annotations;
    ShallowReadingSessionDto readingSession;
}
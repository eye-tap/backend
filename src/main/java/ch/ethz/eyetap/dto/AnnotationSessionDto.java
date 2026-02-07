package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link AnnotationSession}
 */
@Value
public class AnnotationSessionDto implements Serializable {
    Long annotator;
    Set<AnnotationDto> annotations;
    ShallowReadingSessionDto readingSession;
}
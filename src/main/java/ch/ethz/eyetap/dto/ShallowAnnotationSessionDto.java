package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link AnnotationSession}
 */
@Value
public class ShallowAnnotationSessionDto implements Serializable {
    Long id;
    Long annotator;
    AnnotationsMetaDataDto annotationsMetaData;
    ShallowReadingSessionDto readingSession;
}
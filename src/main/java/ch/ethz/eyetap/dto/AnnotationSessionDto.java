package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link AnnotationSession}
 */
public record AnnotationSessionDto(Long id, Long annotator, Set<AnnotationDto> annotations,
                                   AnnotationsMetaDataDto annotationsMetaData,
                                   ReadingSessionDto readingSession) {
}
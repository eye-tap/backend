package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * DTO for {@link AnnotationSession}
 */
public record AnnotationSessionDto(Long id, Long annotator, Set<AnnotationDto> annotations,
                                   AnnotationsMetaDataDto annotationsMetaData,
                                   ReadingSessionDto readingSession,
                                   Map<String, Set<AnnotationDto>> inactiveMachineAnnotations,
                                   LocalDateTime lastEdited,
                                   Set<Long> removedFixations) {
}
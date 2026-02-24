package ch.ethz.eyetap.dto;

import java.util.Set;

/**
 * DTO for import of {@link ch.ethz.eyetap.model.annotation.ReadingSession}
 */
public record ImportReadingSessionDto(Set<ImportFixationDto> fixations,
                                      Long readerForeignId,
                                      Long textForeignId,
                                      String language,
                                      Set<ImportPreAnnotationDto> preAnnotations) {
}
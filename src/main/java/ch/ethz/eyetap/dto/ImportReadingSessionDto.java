package ch.ethz.eyetap.dto;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link ch.ethz.eyetap.model.annotation.ReadingSession}
 */
public record ImportReadingSessionDto(Set<ImportFixationDto> fixations, Long readerForeignId,
                                      Long textForeignId, Long foreignId) implements Serializable {
}
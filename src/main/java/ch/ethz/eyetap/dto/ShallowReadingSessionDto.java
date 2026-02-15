package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.ReadingSession;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link ReadingSession}
 */
public record ShallowReadingSessionDto(
        Long id,
        Long reader,
        Long textId,
        String textTitle,
        LocalDateTime uploadedAt) {
}
package ch.ethz.eyetap.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ch.ethz.eyetap.model.ReadingSession}
 */
@Value
public class ShallowReadingSessionDto implements Serializable {
    Long id;
    Long reader;
    Long text;
}
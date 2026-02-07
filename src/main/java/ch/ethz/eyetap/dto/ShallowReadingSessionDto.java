package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.ReadingSession;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ReadingSession}
 */
@Value
public class ShallowReadingSessionDto implements Serializable {
    Long id;
    Long reader;
    Long textId;
    String textTitle;
}
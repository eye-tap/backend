package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Fixation;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for importing {@link Fixation}
 */
@Value
public class ImportFixationDto implements Serializable {
    Long foreignId;
    Long x;
    Long y;
}
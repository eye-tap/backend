package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Fixation;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Fixation}
 */
@Value
public class FixationDto implements Serializable {
    Long id;
    Long x;
    Long y;
}
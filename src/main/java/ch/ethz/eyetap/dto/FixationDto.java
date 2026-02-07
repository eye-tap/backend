package ch.ethz.eyetap.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ch.ethz.eyetap.model.Fixation}
 */
@Value
public class FixationDto implements Serializable {
    Long id;
    Long x;
    Long y;
}
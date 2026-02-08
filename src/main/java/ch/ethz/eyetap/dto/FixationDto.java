package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Fixation;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Fixation}
 */
public record FixationDto(Long id, Long x, Long y) {
}
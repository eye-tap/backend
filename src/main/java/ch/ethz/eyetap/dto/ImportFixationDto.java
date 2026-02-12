package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Fixation;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for importing {@link Fixation}
 */
public record ImportFixationDto(Long foreignId, Double x, Double y) {
}
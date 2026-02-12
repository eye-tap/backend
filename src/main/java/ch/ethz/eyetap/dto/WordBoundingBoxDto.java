package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.WordBoundingBox;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link WordBoundingBox}
 */
public record WordBoundingBoxDto(Long id,
                                 String word,
                                 Double xMin,
                                 Double xMax,
                                 Double yMin,
                                 Double yMax
) {
}
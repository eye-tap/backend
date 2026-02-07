package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.WordBoundingBox;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link WordBoundingBox}
 */
@Value
public class WordBoundingBoxDto implements Serializable {
    Long id;
    Long boundingBoxXMin;
    Long boundingBoxXMax;
    Long boundingBoxYMin;
    Long boundingBoxYMax;
}
package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link CharacterBoundingBox}
 */
@Value
public class CharacterBoundingBoxDto implements Serializable {
    Long id; // null if import
    String character;
    Long xMin;
    Long xMax;
    Long yMin;
    Long yMax;
}
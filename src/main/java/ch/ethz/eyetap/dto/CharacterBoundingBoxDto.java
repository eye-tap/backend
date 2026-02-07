package ch.ethz.eyetap.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ch.ethz.eyetap.model.CharacterBoundingBox}
 */
@Value
public class CharacterBoundingBoxDto implements Serializable {
    Long id;
    Long xMin;
    Long xMax;
    Long yMin;
    Long yMax;
}
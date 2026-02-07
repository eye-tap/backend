package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.AnnotationType;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link ch.ethz.eyetap.model.Annotation}
 */
@Value
public class AnnotationDto implements Serializable {
    Long id;
    AnnotationType annotationType;
    FixationDto fixation;
    CharacterBoundingBoxDto characterBoundingBox;
}
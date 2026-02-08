package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationType;
import ch.ethz.eyetap.model.annotation.Annotation;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Annotation}
 */
@Value
public class AnnotationDto implements Serializable {
    Long id;
    AnnotationType annotationType;
    ImportFixationDto fixation;
    CharacterBoundingBoxDto characterBoundingBox;
}
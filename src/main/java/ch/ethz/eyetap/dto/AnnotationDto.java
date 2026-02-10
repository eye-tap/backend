package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationType;
import ch.ethz.eyetap.model.annotation.Annotation;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Annotation}
 */
public record AnnotationDto(Long id, AnnotationType annotationType, FixationDto fixation,
                            CharacterBoundingBoxDto characterBoundingBox) {
}
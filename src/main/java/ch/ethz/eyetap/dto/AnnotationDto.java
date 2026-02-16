package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationType;

/**
 * DTO for {@link ch.ethz.eyetap.model.annotation.UserAnnotation} and {@link ch.ethz.eyetap.model.annotation.MachineAnnotation}
 */
public record AnnotationDto(Long id, AnnotationType annotationType, FixationDto fixation,
                            CharacterBoundingBoxDto characterBoundingBox) {
}
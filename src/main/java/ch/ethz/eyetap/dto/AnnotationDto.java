package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.AnnotationType;

/**
 * DTO for {@link Annotation}
 */
public record AnnotationDto(Long id, AnnotationType annotationType, FixationDto fixation,
                            CharacterBoundingBoxDto characterBoundingBox) {
}
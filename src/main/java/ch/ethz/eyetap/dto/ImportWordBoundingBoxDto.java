package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;

/**
 * DTO for {@link ch.ethz.eyetap.model.annotation.WordBoundingBox}
 */
public record ImportWordBoundingBoxDto(Long foreignId,
                                       String word,
                                       Double xMin,
                                       Double xMax,
                                       Double yMin,
                                       Double yMax
) {
}
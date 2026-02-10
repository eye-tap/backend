package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Text;

import java.util.Set;

/**
 * DTO for {@link Text}
 */
public record ImportTextDto(String title, Long foreignId,
                            Set<ImportCharacterBoundingBoxDto> characterBoundingBoxes,
                            Set<ImportWordBoundingBoxDto> wordBoundingBoxes,
                            byte[] backgroundImage) {
}

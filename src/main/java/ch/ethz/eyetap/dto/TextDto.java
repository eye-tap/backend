package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Text;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Text}
 */
public record TextDto(Long id, String title, Set<WordBoundingBoxDto> wordBoundingBoxes,
                      Set<CharacterBoundingBoxDto> characterBoundingBoxes,
                      byte[] backgroundImage) {
}
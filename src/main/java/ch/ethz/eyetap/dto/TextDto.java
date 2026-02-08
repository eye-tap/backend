package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Text;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Text}
 * @param id  null if import
 */
public record TextDto(Long id, String title, Long foreignId, Set<WordBoundingBoxDto> wordBoundingBoxes,
                      Set<CharacterBoundingBoxDto> characterBoundingBoxes,
                      byte[] backgroundImage) {
}
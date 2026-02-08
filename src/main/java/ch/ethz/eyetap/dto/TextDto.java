package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.annotation.Text;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Text}
 */
@Value
public class TextDto implements Serializable {
    Long id; // null if import
    String title;
    Long foreignId;
    Set<WordBoundingBoxDto> wordBoundingBoxes;
    Set<CharacterBoundingBoxDto> characterBoundingBoxes;
    byte[] backgroundImage;
}
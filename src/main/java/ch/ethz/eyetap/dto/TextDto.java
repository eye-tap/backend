package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.model.Text;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Text}
 */
@Value
public class TextDto implements Serializable {
    Long id;
    String title;
    Set<WordBoundingBoxDto> wordBoundingBoxes;
    Set<CharacterBoundingBoxDto> characterBoundingBoxes;
    byte[] backgroundImage;
}
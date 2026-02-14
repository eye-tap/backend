package ch.ethz.eyetap.dto;

import java.util.Map;

public record ImportPreAnnotationDto(
        String title,
        Map<Long, Long> fixationToCharacterBoxForeignIds) {
}

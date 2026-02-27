package ch.ethz.eyetap.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Set;

public record EditAnnotationsDto(

        @Schema(
                description = "Maps fixation IDs to character bounding box IDs",
                example = "{1: 42, 2: 43}"
        )
        Map<Long, Long> annotations,
        Set<Long> fixationsToRemove,
        Map<Long, Long> annotationsToRemove,
        Set<Long> fixationsToUndoRemove) {
}

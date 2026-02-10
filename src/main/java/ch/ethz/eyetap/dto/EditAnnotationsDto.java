package ch.ethz.eyetap.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record EditAnnotationsDto(

        @Schema(
                description = "Maps annotation IDs to character bounding box IDs",
                example = "{1: 42, 2: 43}"
        )
        Map<Long, Long> annotations) {
}

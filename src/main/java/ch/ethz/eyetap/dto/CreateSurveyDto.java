package ch.ethz.eyetap.dto;

import java.util.Set;

public record CreateSurveyDto(
        Long users,
        String title,
        String description,
        Set<Long> readingSessionIds
        // todo: add option for preset of annotations
) {
}

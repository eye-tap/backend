package ch.ethz.eyetap.dto;

import java.util.Set;

public record CreateSurveyDto(
        Long users,
        String title,
        String description,
        Set<Long> readingSessionIds,
        String furtherOptions
) {
}

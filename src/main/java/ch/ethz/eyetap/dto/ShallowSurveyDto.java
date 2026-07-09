package ch.ethz.eyetap.dto;

import ch.ethz.eyetap.dto.progress.ProgressDto;

public record ShallowSurveyDto(Long id, String title, String description,
                               ProgressDto surveyProgressDto) {
}


package ch.ethz.eyetap.dto;

import java.util.Map;

public record SurveyCreatedDto(
        SurveyDto surveyDto,
        Map<String, String> users) {
}

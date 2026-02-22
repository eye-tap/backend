package ch.ethz.eyetap.dto;

public record UserSurveyProgressDto(Long userId, Long total, Long done) {
}

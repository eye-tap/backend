package ch.ethz.eyetap.dto.progress;

import lombok.Builder;

@Builder
public record OverallProgressStatisticsDto(Double progressUntilEverythingIsAnnotatedOnce,
                                           Integer numberOfUniqueAnnotators,
                                           Integer numberOfAnnotations,
                                           Integer numberOfReadingSessions,
                                           Integer numberOfTexts,
                                           Integer numberOfSurveys,
                                           Integer numberOfFixations) {
}

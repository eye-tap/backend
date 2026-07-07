package ch.ethz.eyetap.dto.progress;

public record ReadingSessionProgressDto(Double averageAnnotationsPerFixation,
                                        Integer numberOfUniqueAnnotators) {
}

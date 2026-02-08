package ch.ethz.eyetap.dto;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link ch.ethz.eyetap.model.survey.Survey}
 */
public record SurveyDto(Long id,
                        Set<Long> userIds,
                        String title,
                        String description,
                        Set<ShallowAnnotationSessionDto> annotationSessions) implements Serializable {
}
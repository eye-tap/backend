package ch.ethz.eyetap.model.survey;

import java.util.Set;

public interface SurveyProjection {
    Long getId();
    String getTitle();
    String getDescription();
    Set<Long> getUserIds();
}

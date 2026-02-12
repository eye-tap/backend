package ch.ethz.eyetap.model.survey;

public interface ShallowAnnotationSessionProjection {
    Long getId();
    Long getAnnotatorId();
    Long getReadingSessionId();
    String getReadingSessionTextTitle();
    Long getReadingSessionTextId();
    Long getReadingSessionReaderId();
    int getAnnotationCount();
    int getAnnotatedCount();
}

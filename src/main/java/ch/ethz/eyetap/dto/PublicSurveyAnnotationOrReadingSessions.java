package ch.ethz.eyetap.dto;


import java.util.Map;

// First map contains all reading sessions for which no annotation sessions exist yet
// Second map contains all existing annotation sessions
public record PublicSurveyAnnotationOrReadingSessions(Map<Long, ShallowReadingSessionDto> shallowReadingSessionDtos,
                                                      Map<Long, ShallowAnnotationSessionDto> shallowAnnotationSessionDtos) {
}

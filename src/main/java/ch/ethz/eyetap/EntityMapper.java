package ch.ethz.eyetap;

import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.model.survey.Survey;
import jakarta.persistence.SecondaryTable;
import jakarta.transaction.Transactional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "xMin", source = "boundingBox.XMin")
    @Mapping(target = "xMax", source = "boundingBox.XMax")
    @Mapping(target = "yMin", source = "boundingBox.YMin")
    @Mapping(target = "yMax", source = "boundingBox.YMax")
    CharacterBoundingBoxDto toBoundingBoxDto(CharacterBoundingBox characterBoundingBox);

    @Mapping(target = "xMin", source = "boundingBox.XMin")
    @Mapping(target = "xMax", source = "boundingBox.XMax")
    @Mapping(target = "yMin", source = "boundingBox.YMin")
    @Mapping(target = "yMax", source = "boundingBox.YMax")
    WordBoundingBoxDto toBoundingBoxDto(WordBoundingBox characterBoundingBox);


    @Mapping(target = "textTitle", source = "text.title")
    @Mapping(target = "textId", source = "text.id")
    ShallowReadingSessionDto toShallowReadingSessionDto(ReadingSession readingSession);

    default Long readerId(Reader reader) {
        return reader.getId();
    }

    default Long textId(Text text) {
        return text.getId();
    }

    @Mapping(target = "userIds", expression = "java(userIds(survey))")
    SurveyDto toSurveyDto(Survey survey);

    default Set<Long> userIds(Survey survey) {
        return survey.getUsers().stream().map(User::getId).collect(Collectors.toSet());
    }

    default Long toAnnotatorId(Annotator value) {
        return value.getId();
    }

    @Mapping(target = "annotationsMetaData", source = "annotationsMetaDataDto")
    AnnotationSessionDto toAnnotationSessionDto(AnnotationSession annotationSession, AnnotationsMetaDataDto annotationsMetaDataDto);

    @Mapping(target = "textDto", expression = "java(toTextDto(readingSession.getText()))")
    ReadingSessionDto toReadingSessionDto(ReadingSession readingSession);

    TextDto toTextDto(Text text);
}

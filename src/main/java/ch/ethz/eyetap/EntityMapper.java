package ch.ethz.eyetap;

import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.model.survey.Survey;
import jakarta.persistence.SecondaryTable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "readingSessions", expression = "java(Set.of())")
    Text fromTextDto(TextDto textDto);

    TextDto toTextDto(Text text);

    @Mapping(target = "text", ignore = true)
    @Mapping(target = "boundingBox.XMin", source = "xMin")
    @Mapping(target = "boundingBox.XMax", source = "xMax")
    @Mapping(target = "boundingBox.YMin", source = "yMin")
    @Mapping(target = "boundingBox.YMax", source = "yMax")
    CharacterBoundingBox fromCharacterBoundingBoxDto(CharacterBoundingBoxDto characterBoundingBoxDto);

    @Mapping(target = "xMin", source = "boundingBox.XMin")
    @Mapping(target = "xMax", source = "boundingBox.XMax")
    @Mapping(target = "yMin", source = "boundingBox.YMin")
    @Mapping(target = "yMax", source = "boundingBox.YMax")
    CharacterBoundingBoxDto toBoundingBoxDto(CharacterBoundingBox characterBoundingBox);

    @Mapping(target = "text", ignore = true)
    @Mapping(target = "boundingBox.XMin", source = "xMin")
    @Mapping(target = "boundingBox.XMax", source = "xMax")
    @Mapping(target = "boundingBox.YMin", source = "yMin")
    @Mapping(target = "boundingBox.YMax", source = "yMax")
    WordBoundingBox fromCharacterBoundingBoxDto(WordBoundingBoxDto characterBoundingBoxDto);

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
}

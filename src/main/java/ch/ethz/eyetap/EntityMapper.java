package ch.ethz.eyetap;

import ch.ethz.eyetap.dto.CharacterBoundingBoxDto;
import ch.ethz.eyetap.dto.ShallowReadingSessionDto;
import ch.ethz.eyetap.dto.TextDto;
import ch.ethz.eyetap.dto.WordBoundingBoxDto;
import ch.ethz.eyetap.model.annotation.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "readingSessions", expression = "java(Set.of())")
    Text fromTextDto(TextDto textDto);

    TextDto toTextDto(Text text);

    @Mapping(target = "text", ignore = true)
    @Mapping(target = "boundingBox.XMin", source = "XMin")
    @Mapping(target = "boundingBox.XMax", source = "XMax")
    @Mapping(target = "boundingBox.YMin", source = "YMin")
    @Mapping(target = "boundingBox.YMax", source = "YMax")
    CharacterBoundingBox fromCharacterBoundingBoxDto(CharacterBoundingBoxDto characterBoundingBoxDto);

    @Mapping(target = "xMin", source = "boundingBox.XMin")
    @Mapping(target = "xMax", source = "boundingBox.XMax")
    @Mapping(target = "yMin", source = "boundingBox.YMin")
    @Mapping(target = "yMax", source = "boundingBox.YMax")
    CharacterBoundingBoxDto toBoundingBoxDto(CharacterBoundingBox characterBoundingBox);

    @Mapping(target = "text", ignore = true)
    @Mapping(target = "boundingBox.XMin", source = "XMin")
    @Mapping(target = "boundingBox.XMax", source = "XMax")
    @Mapping(target = "boundingBox.YMin", source = "YMin")
    @Mapping(target = "boundingBox.YMax", source = "YMax")
    WordBoundingBox fromCharacterBoundingBoxDto(WordBoundingBoxDto characterBoundingBoxDto);

    @Mapping(target = "xMin", source = "boundingBox.XMin")
    @Mapping(target = "xMax", source = "boundingBox.XMax")
    @Mapping(target = "yMin", source = "boundingBox.YMin")
    @Mapping(target = "yMax", source = "boundingBox.YMax")
    WordBoundingBoxDto toBoundingBoxDto(WordBoundingBox characterBoundingBox);


    ShallowReadingSessionDto toShallowReadingSessionDto(ReadingSession readingSession);

    default Long readerId(Reader reader) {
        return reader.getId();
    }

    default Long textId(Text text) {
        return text.getId();
    }
}

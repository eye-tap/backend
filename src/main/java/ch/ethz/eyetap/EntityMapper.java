package ch.ethz.eyetap;

import ch.ethz.eyetap.dto.TextDto;
import ch.ethz.eyetap.model.annotation.Text;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "readingSessions", expression = "java(Set.of())")
    Text fromTextDto(TextDto textDto);

    TextDto toTextDto(Text text);

}

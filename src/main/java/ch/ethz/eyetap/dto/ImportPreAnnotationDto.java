package ch.ethz.eyetap.dto;

import java.util.Set;

public record ImportPreAnnotationDto(
        String title,
        Set<PreAnnotationValueDto> annotations) {

}

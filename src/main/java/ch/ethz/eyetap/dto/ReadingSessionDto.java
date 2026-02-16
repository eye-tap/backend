package ch.ethz.eyetap.dto;

import java.util.List;

public record ReadingSessionDto(List<FixationDto> fixations, TextDto textDto) {
}

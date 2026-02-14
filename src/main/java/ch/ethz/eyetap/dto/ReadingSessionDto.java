package ch.ethz.eyetap.dto;

import java.util.Set;

public record ReadingSessionDto(Set<FixationDto> fixations, TextDto textDto) {
}

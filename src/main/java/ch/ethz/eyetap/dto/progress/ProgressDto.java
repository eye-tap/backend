package ch.ethz.eyetap.dto.progress;

import ch.ethz.eyetap.dto.ShallowTextDto;
import lombok.Builder;

import java.util.Map;

@Builder
public record ProgressDto(OverallProgressStatisticsDto statisticsDto,
                          Map<ProgressKey, ReadingSessionProgressDto> progress) {

    public record ProgressKey(Long id, ShallowTextDto shallowText, String language) {

    }

}
package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.ShallowReadingSessionDto;
import ch.ethz.eyetap.service.ReadingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reading-session")
@RequiredArgsConstructor
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;
    private final EntityMapper entityMapper;

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @GetMapping
    public Set<ShallowReadingSessionDto> getAllReadingSessions(
    ) {
        return this.readingSessionService.getAll()
                .stream().map(this.entityMapper::toShallowReadingSessionDto)
                .collect(Collectors.toSet());
    }
}

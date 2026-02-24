package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.annotation.Text;
import ch.ethz.eyetap.service.ReadingSessionService;
import ch.ethz.eyetap.service.TextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {

    private final TextService textService;
    private final ReadingSessionService readingSessionService;

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @PostMapping("/text")
    public ShallowTextDto text(@RequestBody ImportTextDto textDto) {
        log.info("ImportTextDto: {}", textDto);
        Text save = this.textService.save(textDto);
        log.info("Import of text {} done", save.getTitle());
        return new ShallowTextDto(save.getId(), save.getTitle());
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @PostMapping("/reading-session")
    public ShallowReadingSessionDto readingSession(@RequestBody ImportReadingSessionDto importReadingSessionDto) {
        ReadingSession saved = this.readingSessionService.save(importReadingSessionDto);
        return new ShallowReadingSessionDto(saved.getId(),
                saved.getReader().getId(),
                saved.getText().getId(),
                saved.getText().getTitle(),
                saved.getUploadedAt());
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @PostMapping("/reading-sessions")
    public Set<ShallowReadingSessionDto> readingSession(@RequestBody Set<ImportReadingSessionDto> importReadingSessionDto) {
        Set<ShallowReadingSessionDto> readingSessionDtos = new HashSet<>();
        for (ImportReadingSessionDto readingSessionDto : importReadingSessionDto) {
            ReadingSession saved = this.readingSessionService.save(readingSessionDto);
            ShallowReadingSessionDto shallowReadingSessionDto = new ShallowReadingSessionDto(saved.getId(),
                    saved.getReader().getId(),
                    saved.getText().getId(),
                    saved.getText().getTitle(),
                    saved.getUploadedAt());
            readingSessionDtos.add(shallowReadingSessionDto);
        }
        log.info("Import of reading {} sessions complete", readingSessionDtos.size());
        return readingSessionDtos;
    }
}

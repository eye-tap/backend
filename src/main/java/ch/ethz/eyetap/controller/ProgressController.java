package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.progress.ProgressDto;
import ch.ethz.eyetap.service.statistics.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public ProgressDto getProgress() {
        return this.progressService.getProgress();
    }

}

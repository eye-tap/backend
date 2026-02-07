package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.TextDto;
import ch.ethz.eyetap.service.TextService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {

    private final TextService textService;

    @PostMapping("/text")
    public void text(@RequestBody TextDto textDto) {
        this.textService.save(textDto);
    }
}

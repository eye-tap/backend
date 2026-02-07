package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.ShallowTextDto;
import ch.ethz.eyetap.dto.TextDto;
import ch.ethz.eyetap.model.annotation.Text;
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
    public ShallowTextDto text(@RequestBody TextDto textDto) {
        Text save = this.textService.save(textDto);
        return new ShallowTextDto(save.getId(), save.getTitle());
    }
}

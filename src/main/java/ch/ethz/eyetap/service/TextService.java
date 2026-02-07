package ch.ethz.eyetap.service;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.model.Text;
import ch.ethz.eyetap.dto.TextDto;
import ch.ethz.eyetap.repository.TextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TextService {

    private final TextRepository textRepository;
    private final EntityMapper entityMapper;

    public Text save(TextDto textDto) {
        Text text = new Text();

        return this.save(this.entityMapper.fromTextDto(textDto));
    }

    public Text save(Text text) {
        return textRepository.save(text);
    }
}

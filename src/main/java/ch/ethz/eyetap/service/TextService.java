package ch.ethz.eyetap.service;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import ch.ethz.eyetap.model.annotation.Text;
import ch.ethz.eyetap.dto.TextDto;
import ch.ethz.eyetap.model.annotation.WordBoundingBox;
import ch.ethz.eyetap.repository.CharacterBoundingBoxRepository;
import ch.ethz.eyetap.repository.TextRepository;
import ch.ethz.eyetap.repository.WordBoundingBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TextService {

    private final TextRepository textRepository;
    private final EntityMapper entityMapper;
    private final CharacterBoundingBoxRepository characterBoundingBoxRepository;
    private final WordBoundingBoxRepository wordBoundingBoxRepository;

    public Text save(TextDto textDto) {
        return this.save(this.entityMapper.fromTextDto(textDto));
    }

    public Text save(Text text) {
        if (textRepository.existsByTitle(text.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text with this title already exists");
        }
        Set<CharacterBoundingBox> characterBoundingBoxes = new HashSet<>(this.characterBoundingBoxRepository.saveAll(text.getCharacterBoundingBoxes()));
        Set<WordBoundingBox> wordBoundingBoxes = new HashSet<>(this.wordBoundingBoxRepository.saveAll(text.getWordBoundingBoxes()));

        text.setCharacterBoundingBoxes(characterBoundingBoxes);
        text.setWordBoundingBoxes(wordBoundingBoxes);

        return textRepository.save(text);
    }
}

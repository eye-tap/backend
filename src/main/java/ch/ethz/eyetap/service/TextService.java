package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.ImportCharacterBoundingBoxDto;
import ch.ethz.eyetap.dto.ImportTextDto;
import ch.ethz.eyetap.dto.ImportWordBoundingBoxDto;
import ch.ethz.eyetap.model.annotation.BoundingBox;
import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import ch.ethz.eyetap.model.annotation.Text;
import ch.ethz.eyetap.model.annotation.WordBoundingBox;
import ch.ethz.eyetap.repository.TextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TextService {

    private final TextRepository textRepository;

    public Text save(ImportTextDto textDto) {

        Text text = new Text();
        text.setTitle(textDto.title());
        text.setForeignId(textDto.foreignId());
        text.setBackgroundImage(textDto.backgroundImage());

        Set<CharacterBoundingBox> characterBoundingBoxes = new HashSet<>();
        for (ImportCharacterBoundingBoxDto characterBoundingBox : textDto.characterBoundingBoxes()) {
            BoundingBox boundingBox = parseBoundingBox(characterBoundingBox);
            CharacterBoundingBox box = CharacterBoundingBox
                    .builder()
                    .foreignId(characterBoundingBox.foreignId())
                    .boundingBox(boundingBox)
                    .character(characterBoundingBox.character())
                    .text(text)
                    .build();
            characterBoundingBoxes.add(box);
        }
        Set<WordBoundingBox> wordBoundingBoxes = new HashSet<>();
        for (ImportWordBoundingBoxDto wordBoundingBox : textDto.wordBoundingBoxes()) {
            BoundingBox boundingBox = parseBoundingBox(wordBoundingBox);
            WordBoundingBox box = WordBoundingBox.builder()
                    .foreignId(wordBoundingBox.foreignId())
                    .boundingBox(boundingBox)
                    .word(wordBoundingBox.word())
                    .text(text)
                    .build();
            wordBoundingBoxes.add(box);
        }
        text.setCharacterBoundingBoxes(characterBoundingBoxes);
        text.setWordBoundingBoxes(wordBoundingBoxes);

        return this.save(text);
    }

    private static BoundingBox parseBoundingBox(ImportCharacterBoundingBoxDto characterBoundingBox) {
        return BoundingBox.builder().xMin(characterBoundingBox.xMin()).xMax(characterBoundingBox.xMax()).yMin(characterBoundingBox.yMin()).yMax(characterBoundingBox.yMax()).build();
    }

    private static BoundingBox parseBoundingBox(ImportWordBoundingBoxDto wordBoundingBoxDto) {
        return BoundingBox.builder().xMin(wordBoundingBoxDto.xMin()).xMax(wordBoundingBoxDto.xMax()).yMin(wordBoundingBoxDto.yMin()).yMax(wordBoundingBoxDto.yMax()).build();
    }

    public Text save(Text text) {
        if (textRepository.existsByTitle(text.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text with this title already exists");
        }
        return textRepository.save(text);
    }
}

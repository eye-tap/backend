package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.ImportReadingSessionDto;
import ch.ethz.eyetap.model.annotation.Fixation;
import ch.ethz.eyetap.model.annotation.Reader;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.annotation.Text;
import ch.ethz.eyetap.repository.FixationRepository;
import ch.ethz.eyetap.repository.ReaderRepository;
import ch.ethz.eyetap.repository.ReadingSessionRepository;
import ch.ethz.eyetap.repository.TextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final ReaderRepository readerRepository;
    private final TextRepository textRepository;
    private final FixationRepository fixationRepository;

    public ReadingSession save(ImportReadingSessionDto importReadingSessionDto) {

        ReadingSession readingSession = new ReadingSession();

        readingSession.setForeignId(importReadingSessionDto.foreignId());

        Text text = this.textRepository.findByForeignId(importReadingSessionDto.textForeignId());
        readingSession.setText(text);

        readingSession = this.readingSessionRepository.save(readingSession);
        Reader reader;

        if (this.readerRepository.existsByForeignId(importReadingSessionDto.readerForeignId())) {
            reader = this.readerRepository.findByForeignId(importReadingSessionDto.readerForeignId());
        } else {
            reader = new Reader();
            reader.setReadingSessions(Set.of(readingSession));
            reader.setForeignId(importReadingSessionDto.readerForeignId());
            reader = this.readerRepository.save(reader);
        }
        readingSession.setReader(reader);

        readingSession = this.readingSessionRepository.save(readingSession);

        ReadingSession finalReadingSession = readingSession;
        importReadingSessionDto.fixations()
                .stream()
                .filter(fixationDto -> !this.fixationRepository.existsByForeignId(fixationDto.foreignId()))
                .forEach(fixationDto -> {
                    Fixation fixation = new Fixation();
                    fixation.setForeignId(fixationDto.foreignId());
                    fixation.setX(fixationDto.x());
                    fixation.setY(fixationDto.y());
                    fixation.setReadingSession(finalReadingSession);
                    this.fixationRepository.save(fixation);
                });

        Set<Fixation> fixations = importReadingSessionDto.fixations()
                .stream()
                .map(fixationDto -> this.fixationRepository.findByForeignId(fixationDto.foreignId()))
                .collect(Collectors.toSet());

        finalReadingSession.setFixations(fixations);

        return this.readingSessionRepository.save(finalReadingSession);

    }

}



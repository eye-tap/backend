package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.ImportFixationDto;
import ch.ethz.eyetap.dto.ImportReadingSessionDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.Fixation;
import ch.ethz.eyetap.model.annotation.Reader;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.annotation.Text;
import ch.ethz.eyetap.repository.FixationRepository;
import ch.ethz.eyetap.repository.ReaderRepository;
import ch.ethz.eyetap.repository.ReadingSessionRepository;
import ch.ethz.eyetap.repository.TextRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final ReaderRepository readerRepository;
    private final TextRepository textRepository;
    private final FixationRepository fixationRepository;

    @Transactional
    public ReadingSession save(ImportReadingSessionDto importReadingSessionDto) {

        ReadingSession readingSession = new ReadingSession();

        Text text = textRepository.findByForeignId(importReadingSessionDto.textForeignId());
        readingSession.setText(text);

        Reader reader = readerRepository.findByForeignId(importReadingSessionDto.readerForeignId());
        if (reader == null) {
            reader = new Reader();
            reader.setForeignId(importReadingSessionDto.readerForeignId());
            reader = readerRepository.save(reader);
        }

        readingSession.setReader(reader);
        readingSession = readingSessionRepository.save(readingSession);

        // Batch insert fixations
        Set<Long> existingIds = fixationRepository.findAllByIdIsIn(
                importReadingSessionDto.fixations().stream().map(ImportFixationDto::foreignId).toList()
        ).stream().map(Fixation::getForeignId).collect(Collectors.toSet());

        ReadingSession finalReadingSession = readingSession;
        List<Fixation> newFixations = importReadingSessionDto.fixations().stream()
                .filter(f -> !existingIds.contains(f.foreignId()))
                .map(f -> {
                    Fixation fixation = new Fixation();
                    fixation.setForeignId(f.foreignId());
                    fixation.setX(f.x());
                    fixation.setY(f.y());
                    fixation.setReadingSession(finalReadingSession);
                    return fixation;
                }).toList();

        fixationRepository.saveAll(newFixations);

        readingSession.getFixations().addAll(newFixations);
        return readingSession;
    }

    public Set<ReadingSession> getAll() {
        return new HashSet<>(this.readingSessionRepository.findAll());
    }

}



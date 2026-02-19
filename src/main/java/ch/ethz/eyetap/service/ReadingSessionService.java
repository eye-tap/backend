package ch.ethz.eyetap.service;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final ReaderRepository readerRepository;
    private final TextRepository textRepository;
    private final FixationRepository fixationRepository;
    private final CharacterBoundingBoxRepository characterBoundingBoxRepository;
    private final MachineAnnotationRepository machineAnnotationRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public ReadingSession save(ImportReadingSessionDto importReadingSessionDto) {


        ReadingSession readingSession = new ReadingSession();

        Text text = textRepository.findByForeignId(importReadingSessionDto.textForeignId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Text not found"));
        readingSession.setText(text);

        Reader reader = readerRepository.findByForeignId(importReadingSessionDto.readerForeignId());
        if (reader == null) {
            reader = new Reader();
            reader.setForeignId(importReadingSessionDto.readerForeignId());
            reader = readerRepository.save(reader);
        }

        readingSession.setReader(reader);
        readingSession.setUploadedAt(LocalDateTime.now());
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
                    fixation.setDisagreement(f.disagreement());
                    return fixation;
                }).toList();

        fixationRepository.saveAll(newFixations);

        if (importReadingSessionDto.preAnnotations() != null) {
            for (final ImportPreAnnotationDto preAnnotation : importReadingSessionDto.preAnnotations()) {
                String title = preAnnotation.title();
                Set<MachineAnnotation> machineAnnotations = new HashSet<>();
                for (PreAnnotationValueDto preAnnotationValueDto : preAnnotation.annotations()) {
                    MachineAnnotation machineAnnotation = MachineAnnotation.builder()
                            .title(title)
                            .characterBoundingBox(
                                    this.characterBoundingBoxRepository.findById(
                                            preAnnotationValueDto.foreignCharacterBoxId()
                                    ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No character bounding box with id " + preAnnotationValueDto.foreignCharacterBoxId() + " found"))
                            )
                            .fixation(
                                    this.fixationRepository.findById(
                                            preAnnotationValueDto.foreignFixationId()
                                    ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No fixation with id " + preAnnotationValueDto.foreignFixationId() + " found"))
                            )
                            .pShareWeight(preAnnotationValueDto.pShare())
                            .dGeomWeight(preAnnotationValueDto.dGeom())
                            .readingSession(readingSession)
                            .build();
                    machineAnnotations.add(machineAnnotation);
                }
                this.machineAnnotationRepository.saveAll(machineAnnotations);
            }
        }

        readingSession.getFixations().addAll(newFixations);
        return readingSession;
    }

    public Set<ReadingSession> getAll() {
        return new HashSet<>(this.readingSessionRepository.findAll());
    }

    public ShallowReadingSessionDto shallowReadingSessionDto(Long id) {
        return new ShallowReadingSessionDto(id,
                this.readingSessionRepository.findReaderIdByReadingSession(id),
                this.readingSessionRepository.findTextIdByReadingSession(id),
                this.readingSessionRepository.findTextTitleByReadingSession(id),
                this.readingSessionRepository.lastEditedByAnnotationSessionId(id)
        );
    }

    public ReadingSessionDto createReadingSessionDto(ReadingSession readingSession) {
        return new ReadingSessionDto(
                readingSession.getFixations()
                        .stream().map(fixation -> new Tuple(fixation.getForeignId(), new FixationDto(fixation.getId(), fixation.getX(), fixation.getY(), fixation.getDisagreement())))
                        .sorted(Comparator.comparingLong(Tuple::foreignId))
                        .map(Tuple::value)
                        .toList(),
                this.entityMapper.toTextDto(readingSession.getText())
        );
    }

    private record Tuple(Long foreignId, FixationDto value) {
    }
}



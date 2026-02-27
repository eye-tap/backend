package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnotationService {

    private final AnnotationSessionRepository annotationSessionRepository;
    private final UserAnnotationRepository userAnnotationRepository;
    private final CharacterBoundingBoxRepository characterBoundingBoxRepository;
    private final AnnotationSessionService annotationSessionService;
    private final FixationRepository fixationRepository;

    public AnnotationSessionDto annotate(Long sessionId,
                                         Map<Long, Long> annotations,
                                         Map<Long, Long> annotationsToRemove,
                                         Set<Long> fixationsToRemove,
                                         Set<Long> fixationsToUndoRemove) {

        long startTime = System.nanoTime();

        // --- Step 1: Load session ---
        long t1 = System.nanoTime();
        AnnotationSession session = annotationSessionRepository.getReferenceById(sessionId);
        long t2 = System.nanoTime();
        log.info("Loaded AnnotationSession {} in {} ms", sessionId, (t2 - t1) / 1_000_000);

        // --- Step 2: Process new user annotations ---
        long t3 = System.nanoTime();
        Set<UserAnnotation> changes = new HashSet<>();
        for (Map.Entry<Long, Long> entry : annotations.entrySet()) {
            long tStepStart = System.nanoTime();
            Long fixationId = entry.getKey();
            Long characterId = entry.getValue();

            Fixation fixation = fixationRepository.findById(fixationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixation " + fixationId + " not found"));
            CharacterBoundingBox characterBoundingBox = characterBoundingBoxRepository.findById(characterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CharacterBoundingBox " + characterId + " not found"));

            UserAnnotation annotation = findOrCreateAnnotationByFixationIdInSession(fixationId, sessionId)
                    .map(userAnnotation -> {
                        userAnnotation.setCharacterBoundingBox(characterBoundingBox);
                        return userAnnotation;
                    })
                    .orElse(UserAnnotation.builder()
                            .fixation(fixation)
                            .characterBoundingBox(characterBoundingBox)
                            .annotationSession(session)
                            .build());
            changes.add(annotation);
            long tStepEnd = System.nanoTime();
            log.info("Processed annotation for fixation {} -> character {} in {} ms", fixationId, characterId, (tStepEnd - tStepStart) / 1_000_000);
        }
        long t4 = System.nanoTime();
        userAnnotationRepository.saveAll(changes);
        session.getUserAnnotations().addAll(changes);
        log.info("Saved {} user annotations in {} ms", changes.size(), (System.nanoTime() - t4) / 1_000_000);
        log.info("Total annotation processing time: {} ms", (t4 - t3) / 1_000_000);

        // --- Step 3: Remove redundant machine annotations ---
        long t5 = System.nanoTime();
        Set<MachineAnnotation> redundantMachineAnnotations = new HashSet<>();
        for (MachineAnnotation ma : session.getMachineAnnotations()) {
            if (annotations.containsKey(ma.getFixation().getId())) {
                redundantMachineAnnotations.add(ma);
            }
        }
        session.getMachineAnnotations().removeAll(redundantMachineAnnotations);
        for (MachineAnnotation ma : redundantMachineAnnotations) {
            ma.getAnnotationSessions().removeIf(a -> a.getId().equals(sessionId));
        }
        long t6 = System.nanoTime();
        log.info("Removed {} redundant machine annotations in {} ms", redundantMachineAnnotations.size(), (t6 - t5) / 1_000_000);

        // --- Step 4: Remove specific annotations (user + machine) ---
        long t7 = System.nanoTime();
        Set<MachineAnnotation> machineAnnotationsToRemove = new HashSet<>();
        Set<UserAnnotation> userAnnotationsToRemove = new HashSet<>();
        for (Map.Entry<Long, Long> entry : annotationsToRemove.entrySet()) {
            Long fixationId = entry.getKey();
            Long characterId = entry.getValue();
            session.getMachineAnnotations().stream()
                    .filter(ma -> ma.getFixation().getId().equals(fixationId)
                            && ma.getCharacterBoundingBox().getId().equals(characterId))
                    .forEach(machineAnnotationsToRemove::add);
            session.getUserAnnotations().stream()
                    .filter(ua -> ua.getFixation().getId().equals(fixationId)
                            && ua.getCharacterBoundingBox().getId().equals(characterId))
                    .forEach(userAnnotationsToRemove::add);
        }
        session.getMachineAnnotations().removeAll(machineAnnotationsToRemove);
        session.getUserAnnotations().removeAll(userAnnotationsToRemove);
        long t8 = System.nanoTime();
        log.info("Removed {} machine and {} user annotations in {} ms", machineAnnotationsToRemove.size(),
                userAnnotationsToRemove.size(), (t8 - t7) / 1_000_000);

        // --- Step 5: Mark fixations invalid / undo ---
        long t9 = System.nanoTime();
        for (Long fixationId : fixationsToRemove) {
            session.getFixationsMarkedInvalid().add(findOrThrow(fixationId, session.getReadingSession().getFixations()));
        }
        for (Long fixationId : fixationsToUndoRemove) {
            session.getFixationsMarkedInvalid().remove(findOrThrow(fixationId, session.getFixationsMarkedInvalid()));
        }
        long t10 = System.nanoTime();
        log.info("Processed {} fixations to remove and {} to undo in {} ms", fixationsToRemove.size(),
                fixationsToUndoRemove.size(), (t10 - t9) / 1_000_000);

        // --- Step 6: Save session and calculate DTO ---
        long t11 = System.nanoTime();
        session.setLastEdited(LocalDateTime.now());
        AnnotationSession saved = annotationSessionRepository.save(session);
        AnnotationSessionDto dto = annotationSessionService.calculateAnnotationSessionDto(saved);
        long t12 = System.nanoTime();
        log.info("Saved session and calculated DTO in {} ms", (t12 - t11) / 1_000_000);

        log.info("Total annotate() method took {} ms", (t12 - startTime) / 1_000_000);

        return dto;
    }

    private Optional<UserAnnotation> findOrCreateAnnotationByFixationIdInSession(Long fixationId, Long sessionId) {
        return userAnnotationRepository.findByFixationIdAndSessionId(fixationId, sessionId);
    }

    private Fixation findOrThrow(Long fixationId, Set<Fixation> fixations) {
        return fixations.stream()
                .filter(f -> f.getId().equals(fixationId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Fixation with id " + fixationId + " not found in session"));
    }
}
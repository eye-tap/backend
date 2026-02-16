package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnotationService {


    private final AnnotationSessionRepository annotationSessionRepository;
    private final UserAnnotationRepository userAnnotationRepository;
    private final CharacterBoundingBoxRepository characterBoundingBoxRepository;
    private final AnnotationSessionService annotationSessionService;
    private final FixationRepository fixationRepository;
    private final MachineAnnotationRepository machineAnnotationRepository;

    public AnnotationSessionDto annotate(Long sessionId, Map<Long, Long> annotations) {

        AnnotationSession session = this.annotationSessionRepository.getReferenceById(sessionId);

        Set<UserAnnotation> changes = new HashSet<>();
        for (Map.Entry<Long, Long> fixationToCharacterBb : annotations.entrySet()) {
            Fixation fixation = this.fixationRepository.findById(fixationToCharacterBb.getKey()).
                    orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixation with id " + fixationToCharacterBb.getKey() + " found"));
            CharacterBoundingBox characterBoundingBox = this.characterBoundingBoxRepository.findById(fixationToCharacterBb.getValue())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character bounding box with id " + fixationToCharacterBb.getValue() + " found"));

            UserAnnotation annotation = this.findOrCreateAnnotationByFixationIdInSession(fixation.getId(), sessionId)
                    .map(userAnnotation -> {
                        userAnnotation.setCharacterBoundingBox(characterBoundingBox);
                        return userAnnotation;
                    })
                    .orElse(UserAnnotation.builder()
                            .characterBoundingBox(characterBoundingBox)
                            .fixation(fixation)
                            .annotationSession(session)
                            .build()
                    );
            changes.add(annotation);
        }
        userAnnotationRepository.saveAll(changes);
        session.getUserAnnotations().addAll(changes);
        Set<MachineAnnotation> redundantMachineAnnotations = new HashSet<>();
        for (MachineAnnotation machineAnnotation : session.getMachineAnnotations()) {
            if (annotations.containsKey(machineAnnotation.getFixation().getId())) {
                redundantMachineAnnotations.add(machineAnnotation);
            }
        }
        session.getMachineAnnotations().removeAll(redundantMachineAnnotations);
        for (MachineAnnotation redundantMachineAnnotation : redundantMachineAnnotations) {
            redundantMachineAnnotation.getAnnotationSessions().removeIf(annotationSession -> annotationSession.getId().equals(sessionId));
        }
        session.setLastEdited(LocalDateTime.now());
        return this.annotationSessionService.calculateAnnotationSessionDto(annotationSessionRepository.save(session));
    }

    private Optional<UserAnnotation> findOrCreateAnnotationByFixationIdInSession(Long fixationId, Long sessionId) {
        return this.userAnnotationRepository.findByFixationIdAndSessionId(fixationId, sessionId);
    }
}

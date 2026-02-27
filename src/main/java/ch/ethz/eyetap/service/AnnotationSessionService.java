package ch.ethz.eyetap.service;


import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.*;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.*;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotationSessionRepository;
import ch.ethz.eyetap.repository.MachineAnnotationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AnnotationSessionService {

    private final ReadingSessionService readingSessionService;
    private final AnnotationSessionRepository annotationSessionRepository;
    private final EntityMapper entityMapper;
    private final MachineAnnotationRepository machineAnnotationRepository;

    public Set<Long> annotationSessionIdsByUserId(Annotator annotator) {
        return this.annotationSessionRepository.findAllIdsByAnnotator(annotator);
    }

    public UserSurveyProgressDto getAnnotationSessionsUserId(Long userId, Annotator annotator) {
        Set<Long> sessions = this.annotationSessionRepository.findAllIdsByAnnotator(annotator);
        long total = sessions.size();
        long done = 0;
        for (Long sessionId : sessions) {
            if (
                    this.annotationSessionRepository.countSetAnnotationsByAnnotationSessionId(sessionId)
                            == this.annotationSessionRepository.countTotalFixationsByAnnotationSessionId(sessionId)
            ) {
                done++;
            }
        }
        return new UserSurveyProgressDto(userId, total, done);
    }

    public AnnotationsMetaDataDto calculateAnnotationsMetaData(Long annotationSessionId) {
        int total = Math.toIntExact(this.annotationSessionRepository.countTotalFixationsByAnnotationSessionId(annotationSessionId));
        int set = Math.toIntExact(this.annotationSessionRepository.countSetAnnotationsByAnnotationSessionId(annotationSessionId));

        return new AnnotationsMetaDataDto(total, set);
    }

    public ShallowAnnotationSessionDto calculateShallowAnnotationSessionDto(Long annotationSessionId) {
        return new ShallowAnnotationSessionDto(annotationSessionId,
                this.annotationSessionRepository.annotatorByAnnotationSessionId(annotationSessionId),
                this.calculateAnnotationsMetaData(annotationSessionId),
                this.readingSessionService.shallowReadingSessionDto(
                        this.annotationSessionRepository.readingSessionByAnnotationSessionId(annotationSessionId)),
                this.annotationSessionRepository.lastEditedByAnnotationSessionId(annotationSessionId),
                this.annotationSessionRepository.descriptionByAnnotationSessionId(annotationSessionId)
        );
    }

    public AnnotationSession initialize(Survey survey, User user, ReadingSession readingSession) {
        // log.info("Creating annotation sessions for reading session {}", readingSession.getId());
        AnnotationSession annotationSession = new AnnotationSession();
        annotationSession.setAnnotator(user.getAnnotator());
        annotationSession.setReadingSession(readingSession);
        annotationSession.setSurvey(survey);
        annotationSession.setLastEdited(LocalDateTime.now());

        return annotationSession;
    }

    public void delete(AnnotationSession annotationSession) {
        this.annotationSessionRepository.delete(annotationSession);
    }

    public AnnotationSessionDto calculateAnnotationSessionDtoById(final Long id) {
        AnnotationSession referenceById = this.annotationSessionRepository.getReferenceById(id);
        return this.calculateAnnotationSessionDto(referenceById);
    }

    public AnnotationSessionDto calculateAnnotationSessionDto(final AnnotationSession annotationSession) {
        return annotationSessionDto(annotationSession, this.calculateAnnotationsMetaData(annotationSession.getId()));
    }

    private AnnotationSessionDto annotationSessionDto(final AnnotationSession annotationSession, AnnotationsMetaDataDto metaDataDto) {
        Set<AnnotationDto> annotations = new HashSet<>();
        for (UserAnnotation userAnnotation : annotationSession.getUserAnnotations()) {
            AnnotationDto annotationDto = new AnnotationDto(userAnnotation.getId(),
                    AnnotationType.ANNOTATED,
                    new FixationDto(userAnnotation.getFixation().getId(),
                            userAnnotation.getFixation().getX(),
                            userAnnotation.getFixation().getY(),
                            null),
                    this.entityMapper.toBoundingBoxDto(userAnnotation.getCharacterBoundingBox()),
                    null,
                    null);
            annotations.add(annotationDto);
        }

        String activeMachineAnnotation = null;

        for (MachineAnnotation machineAnnotation : annotationSession.getMachineAnnotations()) {
            AnnotationDto annotationDto = new AnnotationDto(machineAnnotation.getId(),
                    AnnotationType.MACHINE_ANNOTATED,
                    new FixationDto(machineAnnotation.getFixation().getId(),
                            machineAnnotation.getFixation().getX(),
                            machineAnnotation.getFixation().getY(),
                            machineAnnotation.getFixation().getDisagreement()),
                    this.entityMapper.toBoundingBoxDto(machineAnnotation.getCharacterBoundingBox()),
                    machineAnnotation.getDGeomWeight(),
                    machineAnnotation.getPShareWeight());
            annotations.add(annotationDto);

            if (activeMachineAnnotation == null) {
                activeMachineAnnotation = machineAnnotation.getTitle();
            }
        }

        List<MachineAnnotation> allMachineAnnotations = this.machineAnnotationRepository.findAllByReadingSessionIds(Collections.singletonList(annotationSession.getReadingSession().getId()));

        Map<String, Set<MachineAnnotation>> machineAnnotationsById = new HashMap<>();
        for (MachineAnnotation allMachineAnnotation : allMachineAnnotations) {
            if (activeMachineAnnotation != null && activeMachineAnnotation.equals(allMachineAnnotation.getTitle())) {
                continue;
            }
            if (!machineAnnotationsById.containsKey(allMachineAnnotation.getTitle())) {
                machineAnnotationsById.put(allMachineAnnotation.getTitle(), new HashSet<>());
            }
            machineAnnotationsById.get(allMachineAnnotation.getTitle()).add(allMachineAnnotation);
        }

        Map<String, Set<AnnotationDto>> machineAnnotationDtos = new HashMap<>();
        for (Map.Entry<String, Set<MachineAnnotation>> stringSetEntry : machineAnnotationsById.entrySet()) {
            String title = stringSetEntry.getKey();
            Set<AnnotationDto> value = new HashSet<>();
            for (MachineAnnotation machineAnnotation : stringSetEntry.getValue()) {
                AnnotationDto annotationDto = new AnnotationDto(machineAnnotation.getId(),
                        AnnotationType.MACHINE_ANNOTATED,
                        new FixationDto(machineAnnotation.getFixation().getId(),
                                machineAnnotation.getFixation().getX(),
                                machineAnnotation.getFixation().getY(),
                                machineAnnotation.getFixation().getDisagreement()),
                        this.entityMapper.toBoundingBoxDto(machineAnnotation.getCharacterBoundingBox()),
                        machineAnnotation.getDGeomWeight(),
                        machineAnnotation.getPShareWeight());
                value.add(annotationDto);
            }

            machineAnnotationDtos.put(title, value);

        }

        Set<Long> removedFixations = new HashSet<>();

        for (Fixation fixation : annotationSession.getFixationsMarkedInvalid()) {
            removedFixations.add(fixation.getId());
        }

        return new AnnotationSessionDto(annotationSession.getId(),
                annotationSession.getAnnotator().getId(),
                annotations,
                metaDataDto,
                this.readingSessionService.createReadingSessionDto(annotationSession.getReadingSession()),
                machineAnnotationDtos,
                annotationSession.getLastEdited(),
                removedFixations);
    }
}

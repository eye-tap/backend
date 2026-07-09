package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.dto.PublicSurveyAnnotationOrReadingSessions;
import ch.ethz.eyetap.dto.ShallowAnnotationSessionDto;
import ch.ethz.eyetap.dto.ShallowReadingSessionDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.model.survey.SurveyType;
import ch.ethz.eyetap.service.AnnotationSessionService;
import ch.ethz.eyetap.service.ReadingSessionService;
import ch.ethz.eyetap.service.SurveyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/annotation/session")
@RequiredArgsConstructor
public class AnnotationSessionController {

    private final AnnotationSessionService annotationSessionService;
    private final SurveyService surveyService;
    private final ReadingSessionService readingSessionService;

    @GetMapping
    public Set<ShallowAnnotationSessionDto> getSessions(
            @AuthenticationPrincipal User user
    ) {
        return this.annotationSessionService.annotationSessionIdsByUserId(user.getAnnotator()).stream()
                .map(this.annotationSessionService::calculateShallowAnnotationSessionDto)
                .collect(Collectors.toSet());
    }

    @GetMapping("/{id}")
    public AnnotationSessionDto getFullAnnotationSessionDto(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (this.annotationSessionService.annotationSessionIdsByUserId(user.getAnnotator())
                .stream().noneMatch(id::equals)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No annotation session with id " + id + " found.");
        }
        return this.annotationSessionService.calculateAnnotationSessionDtoById(id);
    }

    @Transactional
    @GetMapping("survey/{surveyId}")
    public PublicSurveyAnnotationOrReadingSessions getSurveyAnnotationSessionDto(
            @AuthenticationPrincipal User user,
            @PathVariable Long surveyId) {

        Survey survey = this.surveyService.findOrThrowIfNotPresentOrNotPublic(surveyId);


        Set<ShallowAnnotationSessionDto> annotationSessionDtos = this.annotationSessionService.annotationSessionIdsByUserAndSurvey(user.getAnnotator(), survey).stream()
                .map(this.annotationSessionService::calculateShallowAnnotationSessionDto)
                .collect(Collectors.toSet());


        Map<Long, ShallowAnnotationSessionDto> existingAnnotationSessions = annotationSessionDtos.stream()
                .collect(Collectors.toMap(session -> session.readingSession().id(), session -> session));


        Set<Long> toRemove = annotationSessionDtos.stream()
                .map(annotationSessionDto -> annotationSessionDto.readingSession().id())
                .collect(Collectors.toSet());

        Set<ShallowReadingSessionDto> readingSessionDtos = this.readingSessionService.getReadingSessionsOfPublicSurvey(surveyId);
        Map<Long, ShallowReadingSessionDto> readingSessionDtosForWhichNoAnnotationSessionExists =
                readingSessionDtos.stream()
                        .filter(dto -> toRemove.contains(dto.id()))
                        .collect(Collectors.toMap(
                                ShallowReadingSessionDto::id,
                                dto -> dto
                        ));


        return new PublicSurveyAnnotationOrReadingSessions(readingSessionDtosForWhichNoAnnotationSessionExists,
                existingAnnotationSessions);
    }


    @Transactional
    @PostMapping("/survey/{surveyId}/reading_session/{readingSessionId}")
    public AnnotationSessionDto getOrCreateAnnotationSessionForPublicSurvey(
            @AuthenticationPrincipal User user,
            @PathVariable Long surveyId,
            @PathVariable Long readingSessionId
    ) {
        Survey survey = this.surveyService.findOrThrowIfNotPresentOrNotPublic(surveyId);

        ReadingSession readingSession = this.readingSessionService.getOrThrow(readingSessionId);

        AnnotationSession annotationSession = this.annotationSessionService.getOrCreate(user.getAnnotator(),
                survey,
                readingSession
        );
        return this.annotationSessionService.calculateAnnotationSessionDto(annotationSession);


    }


}

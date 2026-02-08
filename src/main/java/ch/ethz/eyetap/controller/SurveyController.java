package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.CreateSurveyDto;
import ch.ethz.eyetap.dto.SurveyCreatedDto;
import ch.ethz.eyetap.dto.SurveyDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final EntityMapper entityMapper;

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @PostMapping
    public SurveyCreatedDto create(@RequestBody CreateSurveyDto createSurveyDto,
                                   @AuthenticationPrincipal User user) {
        return this.surveyService.create(user, createSurveyDto);
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @GetMapping("/{id}")
    public SurveyDto get(@PathVariable("id") Long id) {
        return this.entityMapper.toSurveyDto(this.surveyService.getById(id));
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @GetMapping
    public Set<SurveyDto> getAll() {
        return this.surveyService.getAll()
                .stream()
                .map(this.entityMapper::toSurveyDto)
                .collect(Collectors.toSet());
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id,
                       @AuthenticationPrincipal User user) {
        Survey survey = this.surveyService.getById(id);
        if (survey.getAdmin().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
            log.warn("User {} tried to delete foreign survey with id {}, this will later be removed!", user.getId(), id);
        }
        this.surveyService.delete(id);
    }
}

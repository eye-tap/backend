package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.CreateSurveyDto;
import ch.ethz.eyetap.dto.SurveyCreatedDto;
import ch.ethz.eyetap.dto.SurveyDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.service.HibernateStatisticsPrinter;
import ch.ethz.eyetap.service.SurveyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final EntityMapper entityMapper;
    private final SessionFactory sessionFactory;

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @PostMapping
    public SurveyCreatedDto create(@RequestBody CreateSurveyDto createSurveyDto,
                                   @AuthenticationPrincipal User user) {
        SurveyCreatedDto surveyCreatedDto = this.surveyService.create(user, createSurveyDto);
        HibernateStatisticsPrinter.print(this.sessionFactory.getStatistics());
        return surveyCreatedDto;
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @GetMapping("/{id}")
    public SurveyDto get(@PathVariable("id") Long id,
                         @AuthenticationPrincipal User user) {
        Survey survey = this.surveyService.getById(id);
        if (survey.getAdmin().stream().map(User::getId)
                .noneMatch(adminId -> adminId.equals(user.getId()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested survey doesnt exist");
        }
        return this.entityMapper.toSurveyDto(survey);
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @GetMapping
    @Transactional
    public Set<SurveyDto> getAll(@AuthenticationPrincipal User user) {
        Set<SurveyDto> collect = this.surveyService.getAll(user.getId());
        HibernateStatisticsPrinter.print(this.sessionFactory.getStatistics());
        return collect;
    }

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable("id") Long id,
                       @AuthenticationPrincipal User user) {
        Survey survey = this.surveyService.getById(id);
        if (!this.surveyService.hasAccessToSurvey(user.getId(), survey)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested survey doesnt exist");
        }
        this.surveyService.delete(id);
    }
}

package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.UserSurveyProgressDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.Annotator;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.AnnotatorRepository;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.service.AnnotationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {


    private final SurveyRepository surveyRepository;
    private final AnnotationSessionService annotationSessionService;
    private final AnnotatorRepository annotatorRepository;

    @GetMapping("/survey/{id}/")
    public List<UserSurveyProgressDto> getUserSurveyProgress(@PathVariable Long id) {
        Survey survey = this.surveyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey with id " + id + " not found"));
        List<UserSurveyProgressDto> progressDtos = new ArrayList<>();
        for (User user : survey.getUsers()) {
            Annotator annotator = user.getAnnotator();
            UserSurveyProgressDto progress = this.annotationSessionService.getAnnotationSessionsUserId(user.getId(), annotator);
            progressDtos.add(progress);
        }
        progressDtos.sort(Comparator.comparingLong(UserSurveyProgressDto::userId));
        return progressDtos;
    }

    @GetMapping("/options")
    public String getUserOptions(@AuthenticationPrincipal User user) {
        return user.getAnnotator().getFurtherOptions();
    }

    @PostMapping("/options")
    public String setUserOptions(@AuthenticationPrincipal User user,
                                 @RequestBody String options) {
        Annotator annotator = user.getAnnotator();
        annotator.setFurtherOptions(options);
        return annotatorRepository.save(annotator).getFurtherOptions();
    }

}

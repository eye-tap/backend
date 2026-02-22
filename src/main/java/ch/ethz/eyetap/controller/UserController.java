package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.UserSurveyProgressDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.Annotator;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.service.AnnotationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {


    private final SurveyRepository surveyRepository;
    private final AnnotationSessionService annotationSessionService;

    @GetMapping("/{id}/")
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

}

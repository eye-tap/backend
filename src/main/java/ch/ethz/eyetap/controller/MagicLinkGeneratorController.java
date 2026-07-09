package ch.ethz.eyetap.controller;


import ch.ethz.eyetap.dto.CreateSurveyDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.repository.UserRepository;
import ch.ethz.eyetap.service.AuthService;
import ch.ethz.eyetap.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/generator")
@RequiredArgsConstructor
public class MagicLinkGeneratorController {
    private final SurveyService surveyService;
    private final AuthService authService;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    // todo: this is insecure as of now, fix later
    @PostMapping("/draw/{surveyId}")
    public AuthService.SurveyParticipant draw(@PathVariable Long surveyId) {
        Survey survey = this.surveyService.getById(surveyId);
        List<String> pseudonyms = this.surveyService.generateFreshPseudonyms(1);
        List<AuthService.SurveyParticipant> surveyParticipantsBatch = this.authService.createSurveyParticipantsBatch(pseudonyms);
        AuthService.SurveyParticipant participant = surveyParticipantsBatch.getFirst();

        survey.getUsers().add(participant.user());
        participant.user().setSurveys(Set.of(survey));

        this.userRepository.save(participant.user());
        this.surveyRepository.save(survey);

        return participant;
    }

}

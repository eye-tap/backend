package ch.ethz.eyetap.controller;


import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.service.SurveyService;
import ch.ethz.eyetap.service.export.ExportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final SurveyRepository surveyRepository;
    private final SurveyService surveyService;

    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @GetMapping("/survey/{id}")
    @Transactional
    public ResponseEntity<byte[]> exportSurvey(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {

        Survey survey = surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));
        if (!this.surveyService.hasAccessToSurvey(user.getId(), survey)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found");
        }
        byte[] csvData = exportService.exportSurvey(survey);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=survey.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csvData);
    }

}

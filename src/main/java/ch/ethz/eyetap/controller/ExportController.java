package ch.ethz.eyetap.controller;


import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.repository.SurveyRepository;
import ch.ethz.eyetap.service.export.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final SurveyRepository surveyRepository;


    @GetMapping("/survey/{id}")
    public ResponseEntity<byte[]> exportSurvey(@PathVariable Long id) {

        Survey survey = surveyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));
        byte[] csvData = exportService.exportSurvey(survey);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=survey.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csvData);
    }

}

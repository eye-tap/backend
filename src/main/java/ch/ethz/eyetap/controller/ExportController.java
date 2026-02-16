package ch.ethz.eyetap.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {


    @PostMapping("survey/{id}")
    public void exportSurvey(@PathVariable Long id) {

    }
}

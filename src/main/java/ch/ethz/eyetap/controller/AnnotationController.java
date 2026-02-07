package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.service.AnnotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/annotation")
@RequiredArgsConstructor
public class AnnotationController {
    public final AnnotationService annotationService;



}

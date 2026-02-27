package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.dto.EditAnnotationsDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.service.AnnotationService;
import ch.ethz.eyetap.service.AnnotationSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

@RestController
@RequestMapping("/annotation")
@RequiredArgsConstructor
public class AnnotationController {
    public final AnnotationService annotationService;
    private final AnnotationSessionService annotationSessionService;

    @PostMapping("/{sessionId}")
    public AnnotationSessionDto annotate(@RequestBody EditAnnotationsDto annotations,
                                         @PathVariable Long sessionId,
                                         @AuthenticationPrincipal User user) {
        this.annotationSessionService.annotationSessionIdsByUserId(user.getAnnotator())
                .stream()
                .filter(annotationSession -> Objects.equals(annotationSession, sessionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You dont have access to this session or it does not exist"));
        return this.annotationService.annotate(sessionId, annotations.annotations() == null ? new HashMap<>() : annotations.annotations(),
                annotations.annotationsToRemove() == null ? new HashMap<>() : annotations.annotationsToRemove(),
                annotations.fixationsToRemove() == null ? new HashSet<>() : annotations.fixationsToRemove(),
                annotations.fixationsToUndoRemove() == null ? new HashSet<>() : annotations.fixationsToUndoRemove());

    }

}

package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.dto.EditAnnotationsDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.service.AnnotationService;
import ch.ethz.eyetap.service.AnnotationSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/annotation")
@RequiredArgsConstructor
public class AnnotationController {
    public final AnnotationService annotationService;
    private final AnnotationSessionService annotationSessionService;
    private final EntityMapper entityMapper;

    @PostMapping("/{sessionId}")
    public AnnotationSessionDto annotate(@RequestBody EditAnnotationsDto annotations,
                                         @PathVariable Long sessionId,
                                         @AuthenticationPrincipal User user) {
        AnnotationSession session = this.annotationSessionService.getAnnotationSessionsByUser(user)
                .stream()
                .filter(annotationSession -> annotationSession.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You dont have access to this session or it does not exist"));
        AnnotationSession updatedSession = this.annotationService.annotate(session, annotations.annotations());
        return this.entityMapper.toAnnotationSessionDto(updatedSession, this.annotationSessionService.calculateAnnotationsMetaData(updatedSession));
    }

}

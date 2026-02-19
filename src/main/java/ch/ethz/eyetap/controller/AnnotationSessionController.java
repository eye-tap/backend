package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.dto.ShallowAnnotationSessionDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.service.AnnotationSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/annotation/session")
@RequiredArgsConstructor
public class AnnotationSessionController {

    private final AnnotationSessionService annotationSessionService;

    @GetMapping
    public Set<ShallowAnnotationSessionDto> getSessions(
            @AuthenticationPrincipal User user
    ) {
        return this.annotationSessionService.annotationSessionIdsByUserId(user.getAnnotator()).stream()
                .map(this.annotationSessionService::calculateShallowAnnotationSessionDto)
                .collect(Collectors.toSet());
    }

    @GetMapping("/{id}")
    public AnnotationSessionDto getFullAnnotationSessionDto(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        if (this.annotationSessionService.annotationSessionIdsByUserId(user.getAnnotator())
                .stream().noneMatch(id::equals)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No annotation session with id " + id + " found.");
        }
        return this.annotationSessionService.calculateAnnotationSessionDtoById(id);
    }

}

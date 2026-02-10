package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.EntityMapper;
import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.dto.AnnotationsMetaDataDto;
import ch.ethz.eyetap.dto.ShallowAnnotationSessionDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.service.AnnotationSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/annotation/session")
@RequiredArgsConstructor
public class AnnotationSessionController {

    private final AnnotationSessionService annotationSessionService;
    private final EntityMapper entityMapper;

    @GetMapping
    public Set<ShallowAnnotationSessionDto> getSessions(
            @AuthenticationPrincipal User user
    ) {
        // Get roles/authorities of the current user
        Set<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        System.out.println("Current user: " + user.getUsername());
        System.out.println("Roles: " + roles);

        return this.annotationSessionService.getAnnotationSessionsByUser(user).stream()
                .map(session -> {
                    AnnotationsMetaDataDto meta = this.annotationSessionService.calculateAnnotationsMetaData(session);
                    return new ShallowAnnotationSessionDto(
                            session.getId(),
                            session.getAnnotator().getId(),
                            meta,
                            this.entityMapper.toShallowReadingSessionDto(session.getReadingSession())
                    );
                }).collect(Collectors.toSet());

    }

    @GetMapping("/{id}")
    public AnnotationSessionDto getFullAnnotationSessionDto(
            @RequestParam Long id,
            @AuthenticationPrincipal User user) {
        AnnotationSession session = this.annotationSessionService.getAnnotationSessionsByUser(user)
                .stream().filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No annotation session with id " + id + " found."));
        return this.entityMapper.toAnnotationSessionDto(session,
                this.annotationSessionService.calculateAnnotationsMetaData(session));
    }

}

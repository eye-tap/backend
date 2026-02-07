package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.AnnotationSessionDto;
import ch.ethz.eyetap.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/annotation/session")
@RequiredArgsConstructor
public class AnnotationSessionController {

    @GetMapping
    public Set<AnnotationSessionDto> getSessions(
            @AuthenticationPrincipal User user
    ) {
        // Get roles/authorities of the current user
        Set<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        System.out.println("Current user: " + user.getUsername());
        System.out.println("Roles: " + roles);

        return Set.of(); // TODO: map user sessions
    }
}

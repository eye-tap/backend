package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.dto.AuthRequest;
import ch.ethz.eyetap.dto.AuthResponse;
import ch.ethz.eyetap.dto.SignupRequest;
import ch.ethz.eyetap.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse signup(@RequestBody SignupRequest req) {
        return new AuthResponse(authService.signup(req.id(), req.email(), req.password(), req.accountType()));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req) {
        return new AuthResponse(authService.login(req.id(), req.password()));
    }
}

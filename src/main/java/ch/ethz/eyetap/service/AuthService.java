package ch.ethz.eyetap.service;

import ch.ethz.eyetap.model.Annotator;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.repository.AnnotatorRepository;
import ch.ethz.eyetap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AnnotatorRepository annotatorRepository;
    private final PasswordEncoder encoder;

    public String signup(String email, String password) {
        if (this.userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));

        user = this.userRepository.save(user);

        Annotator annotator = new Annotator();
        annotator.setUser(user);
        annotator = this.annotatorRepository.save(annotator);

        user.setAnnotator(annotator);
        user = this.userRepository.save(user);

        return jwtService.generateToken(user);
    }

    public String login(String email, String password) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, password));

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return jwtService.generateToken(user);
    }
}

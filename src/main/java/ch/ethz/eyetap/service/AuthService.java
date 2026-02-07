package ch.ethz.eyetap.service;

import ch.ethz.eyetap.model.annotation.Annotator;
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
    private final PasswordGeneratorService passwordGeneratorService;

    public String signup(String username, String email, String password) {
        if (this.userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user = createAnnotator(password, user, "SURVEY_ADMIN");

        return jwtService.generateToken(user);
    }

    private User createAnnotator(String password, User user, String role) {
        user.setPassword(encoder.encode(password));
        user.setRole(role);

        user = this.userRepository.save(user);

        Annotator annotator = new Annotator();
        annotator.setUser(user);
        annotator = this.annotatorRepository.save(annotator);

        user.setAnnotator(annotator);
        user = this.userRepository.save(user);
        return user;
    }

    public String createSurveyParticipant(String userName) {
        User user = new User();
        user.setUsername(userName);
        String password = this.passwordGeneratorService.genPassword(8);
        createAnnotator(password, user, "SURVEY_PARTICIPANT");
        return password;
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

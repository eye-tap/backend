package ch.ethz.eyetap.service;

import ch.ethz.eyetap.dto.AccountType;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.Annotator;
import ch.ethz.eyetap.repository.AnnotatorRepository;
import ch.ethz.eyetap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AnnotatorRepository annotatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    private static final String ROLE_ADMIN = "SURVEY_ADMIN";
    private static final String ROLE_PARTICIPANT = "SURVEY_PARTICIPANT";
    private static final String ROLE_CROWD_SOURCE = "SURVEY_CROWD_SOURCE";

    /**
     * Sign up a new admin user.
     */
    @Transactional
    public String signup(String username, String email, String password, final AccountType accountType) {
        checkUsernameAndEmailAvailable(username, email);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        String role = switch (accountType) {
            case SURVEY_ADMIN -> ROLE_ADMIN;
            case SURVEY_PARTICIPANT ->
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not manually register as a survey participant");
            case CROWD_SOURCE -> ROLE_CROWD_SOURCE;
        };
        user = createUserWithRole(user, password, role);
        return jwtService.generateToken(user);
    }

    /**
     * Creates a new survey participant with a generated password.
     */
    @Transactional
    public SurveyParticipant createSurveyParticipant(String username) {
        User user = new User();
        user.setUsername(username);

        String password = passwordGeneratorService.genPassword(8);

        user = createUserWithRole(user, password, ROLE_PARTICIPANT);

        return new SurveyParticipant(user, password);
    }

    /**
     * Authenticate a user and return a JWT token.
     */
    public String login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = (User) auth.getPrincipal();

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return jwtService.generateToken(user);
    }

    // --------------------- PRIVATE HELPERS ---------------------

    private void checkUsernameAndEmailAvailable(String username, String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already in use");
        }
    }

    /**
     * Create a user and corresponding annotator.
     */
    @Transactional
    public User createUserWithRole(User user, String rawPassword, String role) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        // Save user first
        User savedUser = userRepository.save(user);

        // Create annotator and link
        Annotator annotator = new Annotator();
        annotator.setUser(savedUser);
        annotator = annotatorRepository.save(annotator);

        savedUser.setAnnotator(annotator);

        // Save user again with annotator link
        return userRepository.save(savedUser);
    }

    public record SurveyParticipant(User user, String password) {
    }
}

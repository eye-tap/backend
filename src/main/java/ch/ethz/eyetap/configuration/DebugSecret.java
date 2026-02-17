package ch.ethz.eyetap.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DebugSecret {

    private final String key;

    public DebugSecret(@Value("${debug.secret}") String secret) {
        this.key = secret;
    }
}

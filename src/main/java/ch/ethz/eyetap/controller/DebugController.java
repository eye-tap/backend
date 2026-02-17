package ch.ethz.eyetap.controller;

import ch.ethz.eyetap.configuration.DebugSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final DebugSecret debugSecret;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/data")
    public void deleteData(@RequestBody String key) {

        if (!key.equalsIgnoreCase(debugSecret.getKey())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        jdbcTemplate.execute("""
            DO $$ DECLARE
                r RECORD;
            BEGIN
                EXECUTE (
                    SELECT 'TRUNCATE TABLE ' ||
                           string_agg(format('%I.%I', schemaname, tablename), ', ') ||
                           ' RESTART IDENTITY CASCADE'
                    FROM pg_tables
                    WHERE schemaname = 'public'
                );
            END $$;
        """);
    }
}

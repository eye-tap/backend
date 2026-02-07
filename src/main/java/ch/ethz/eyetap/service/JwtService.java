package ch.ethz.eyetap.service;

import ch.ethz.eyetap.configuration.JwtSecret;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtSecret jwtSecret;

    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 1 day

    public String generateToken(UserDetails user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUsername());
        claims.put("email", user.getUsername());
        claims.put("username", user.getUsername());
        claims.put("exp", System.currentTimeMillis() + EXPIRATION_MS);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(jwtSecret.getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public boolean validateToken(String token, UserDetails user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecret.getKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }
}

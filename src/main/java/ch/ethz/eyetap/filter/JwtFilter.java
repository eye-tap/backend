package ch.ethz.eyetap.filter;

import ch.ethz.eyetap.service.CustomUserDetailsService;
import ch.ethz.eyetap.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        log.debug("[JwtFilter] Request URI: {}", request.getRequestURI());
        log.debug("[JwtFilter] Authorization header: {}", authHeader);

        final String token;
        String username;

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            log.debug("[JwtFilter] No Bearer token, skipping filter");
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);
        log.debug("[JwtFilter] Extracted token: {}", token);

        try {
            username = jwtService.extractUsername(token);
            log.debug("[JwtFilter] Extracted username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("[JwtFilter] Loaded userDetails: {}", userDetails.getUsername());

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JwtFilter] SecurityContext set for user: {}", userDetails.getUsername());
                } else {
                    log.debug("[JwtFilter] Token invalid or expired for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("[JwtFilter] Exception during JWT processing: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}

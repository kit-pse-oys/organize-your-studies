package de.pse.oys.security;

import de.pse.oys.service.auth.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter zur Validierung von JWT-Tokens bei jedem eingehenden Request.
 * Extrahiert die Benutzer-ID und hinterlegt sie im SecurityContext.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    /**
     * Stellt den JwtProvider bereit, der für die Validierung und Extraktion von Informationen aus dem JWT verantwortlich ist.
     * @param jwtProvider der JWT Provider, der die Logik zur Token-Validierung und -Extraktion implementiert.
     */
    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal (HttpServletRequest request,@NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtProvider.validateToken(token)) {
                // HIER PASSIERT ES: Die UUID wird extrahiert
                UUID userId = jwtProvider.extractUserId(token);

                // Ein neues Principal-Objekt mit der UUID wird erstellt
                UserPrincipal principal = new UserPrincipal(userId, "User", "Password");

                // Die Authentifizierung wird für Spring Security "amtlich" gemacht
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                sendUnauthorizedResponse(response, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Standard 401 unauthorized Statuscode
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }
}
package de.pse.oys.security;

import de.pse.oys.service.auth.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * JwtFilterTest – Unit-Tests für die JwtFilter-Klasse.
 *
 * @author uhupo
 * @version 1.0
 */
class JwtFilterTest {

    private JwtProvider jwtProvider;
    private JwtFilter jwtFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtProvider = mock(JwtProvider.class);
        jwtFilter = new JwtFilter(jwtProvider);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldSendUnauthorized() throws Exception {
        // Ein Header mit einem Token, das aber vom Provider als ungültig abgelehnt wird
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtProvider.validateToken(invalidToken)).thenReturn(false);

        // Writer fängt den Output ab
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // WHEN: Filter ausführen
        jwtFilter.doFilterInternal(request, response, filterChain);

        // THEN:
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(stringWriter.toString().contains("Invalid JWT token"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_withNoHeader_shouldContinueChain() throws Exception {
        // Test für den Fall ohne Authorization Header
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtProvider, never()).validateToken(anyString());
    }
}

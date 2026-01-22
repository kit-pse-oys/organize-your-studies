package de.pse.oys.service.auth;

import de.pse.oys.dto.response.AuthResponseDTO;
import de.pse.oys.dto.LoginDTO;
import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.persistence.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService – Der Service für die Identitäts- und zentrale Sitzungsverwaltung.
 * Seine Kernfunktionen umfassen:
 *  - Provider-Management: Verwaltung der lokalen und externen Authentifizierung (OAuth2).
 *  - Validierung externer Identitäten: Sicherstellung der Authentizität externer Benutzer.
 *  - Sitzungsverwaltung(JWT): Nach erfolgreicher Authentifizierung Erzeugung von Sitzungstokens.
 *
 * @author uhupo
 * @version 1.0
 */

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh.token.expiration}")
    private long expirationRefreshToken;

    /**
     * Konstruktor mit Dependency Injection.
     * @param userRepository Das UserRepository für den Zugriff auf Benutzerdaten.
     *
     * @param passwordEncoder
     * @param jwtProvider
     * @param expirationRefreshToken
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       @Value("${jwt.refresh-token-expiration}") long expirationRefreshToken) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.expirationRefreshToken = expirationRefreshToken;
    }


    public AuthResponseDTO login(LoginDTO loginDTO) {
        //TODO
        return null;
    }

    public void logout(RefreshTokenDTO refreshTokenDTO) {
        //TODO
        // Erklärt die JWT für ungültig und entfernt sie aus dem Client-Speicher.

    }

    /**
     * Erneuert den Access-Token (das JWT) basierend auf der Gültigkeit des bereitgestellten Refresh-Tokens.
     * Wenn das Refresh-Token gültig ist, wird ein neues JWT generiert und zurückgegeben.
     * <p>
     * WICHTIG: Das Refresh-Token selbst wird nicht erneuert oder geändert.
     * Es bleibt bis zu seinem ursprünglichen Ablaufdatum gültig.
     *
     * @return
     */
    public AuthResponseDTO refreshToken() {
        //TODO
        return null;
    }

    private void createAuthResponse() {
        //TODO
    }

    private void validateExternalToken() {
        //TODO
    }
}

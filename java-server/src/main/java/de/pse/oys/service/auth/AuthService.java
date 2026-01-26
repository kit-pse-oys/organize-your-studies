package de.pse.oys.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import de.pse.oys.domain.ExternalUser;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * AuthService – Der Service für die Identitäts- und zentrale Sitzungsverwaltung.
 * Seine Kernfunktionen umfassen:
 * - Provider-Management: Verwaltung der lokalen und externen Authentifizierung (OAuth2).
 * - Validierung externer Identitäten: Sicherstellung der Authentizität externer Benutzer.
 * - Sitzungsverwaltung(JWT): Nach erfolgreicher Authentifizierung Erzeugung von Sitzungstokens.
 *
 * @author uhupo
 * @version 1.0
 */

@Service
public class AuthService {
    private static final String ERR_UNSUPPORTED_AUTH_PROVIDER = "Nicht unterstützter Authentifizierungsanbieter: %s";
    private static final String ERR_LOCAL_USER_INCONSISTENT = "Der Benutzername existiert, " +
            "findet aber keinen zugehörigen lokalen Benutzer in der Datenbank. ";
    private static final String ERR_INVALID_LOGIN_CREDENTIALS = "Ungültige Anmeldeinformationen.";
    private static final String ERR_INVALID_EXTERNAL_TOKEN = "Ungültiges externes Token übermittelt.";
    private static final String STANDARD_GOOGLE_USER_NAME = "Google User";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final GoogleOAuthVerifier googleOAuthVerifier;

    /**
     * Konstruktor mit Dependency Injection.
     *
     * @param userRepository      Das UserRepository für den Zugriff auf Benutzerdaten.
     * @param passwordEncoder     Der PasswordEncoder für die Passwort-Hashing und -Validierung.
     * @param jwtProvider         Der JwtProvider für die JWT-Erstellung und -Validierung.
     * @param googleOAuthVerifier Verifier für Google OAuth2 Tokens.
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       GoogleOAuthVerifier googleOAuthVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.googleOAuthVerifier = googleOAuthVerifier;
    }


    /**
     * Authentifiziert einen Benutzer basierend auf den bereitgestellten Anmeldeinformationen.
     * Unterstützt sowohl lokale (Benutzername/Passwort) als auch externe Authentifizierungsmethoden.
     * Implementiert Just-in-Time-Provisioning für externe Benutzer.
     *
     * @param loginDTO Die Anmeldeinformationen des Benutzers.
     * @return AuthResponseDTO mit Access- und Refresh-Token bei erfolgreicher Authentifizierung.
     * @throws IllegalStateException    Geworfen, wenn eine unauflösbare Inkonsistenz zwischen DTO und Datenbank besteht.
     * @throws IllegalArgumentException Geworfen, wenn die Authentifizierung fehlschlägt (z.B. ungültiges Passwort).
     */
    @Transactional
    public AuthResponseDTO login(LoginDTO loginDTO) throws IllegalStateException, IllegalArgumentException {
        // Unterscheidung zwischen lokalen und externen Benutzern.
        AuthType authType = loginDTO.getAuthType();

        if (authType == AuthType.BASIC) {
            return authLocalUser(loginDTO);
        } else {
            return authExternalUser(loginDTO);
        }
    }

    private AuthResponseDTO authExternalUser(LoginDTO loginDTO) {
        String externalToken = loginDTO.getExternalToken();
        UserType authProvider = loginDTO.getAuthProvider();

        // 1. Token mit dem entsprechenden Verifier überprüfen.
        if (UserType.GOOGLE == authProvider) {
            GoogleIdToken.Payload payload = googleOAuthVerifier.verifyToken(externalToken);
            if (payload == null) {
                throw new IllegalArgumentException(ERR_INVALID_EXTERNAL_TOKEN);
            }
            // 2. Extrahieren der Benutzerinformationen aus der Payload.
            String name = payload.get("name") != null ? payload.get("name").toString() : STANDARD_GOOGLE_USER_NAME;
            String googleSub = payload.getSubject(); // Eindeutige Google-Benutzer-ID

            // 3. Benutzer in der Datenbank suchen
            Optional<ExternalUser> optionalUser = userRepository.findByExternalSubjectIdAndType(googleSub, UserType.GOOGLE);

            User user;
            if (optionalUser.isPresent()) {
                // Benutzer existiert bereits, JWT und Refresh-Token generieren.
                user = optionalUser.get();
            } else {
                // 4. Benutzer existiert nicht, neuen Benutzer anlegen (Just-in-Time-Provisioning).
                user = new ExternalUser(UUID.randomUUID(), name, googleSub, UserType.GOOGLE);
            }
            // 5. JWT und Refresh-Token generieren.
            String accessToken = jwtProvider.createAccessToken(user);
            String refreshToken = jwtProvider.createRefreshToken(user);

            //6. Refresh-Token in der Datenbank speichern
            user.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
            userRepository.save(user);

            // 7. AuthResponseDTO zurückgeben.
            return new AuthResponseDTO(accessToken, refreshToken, user.getId(), name);

        } else {
            // Nicht unterstützter Authentifizierungsanbieter.
            throw new IllegalArgumentException(String.format(ERR_UNSUPPORTED_AUTH_PROVIDER, authProvider));
        }
    }

    private AuthResponseDTO authLocalUser(LoginDTO loginDTO) throws IllegalStateException, IllegalArgumentException {
        String username = loginDTO.getUsername();

        // 1. Überprüfen, ob der Benutzer existiert und entsprechend den User abrufen.

        Optional<User> optionalUser = userRepository.findByNameAndType(username, de.pse.oys.domain.enums.UserType.LOCAL);
        LocalUser user = (LocalUser) optionalUser.orElseThrow(() -> new IllegalStateException(ERR_LOCAL_USER_INCONSISTENT));// Stellt Konsistenz zwischen DTO und DB sicher

        // 2. Passwort validieren.
        String userSalt = user.getSalt(); // Salt abrufen
        String hashedPassword = user.getHashedPassword(); // Hash abrufen

        if (!passwordEncoder.matches(loginDTO.getPassword() + userSalt, hashedPassword)) {
            throw new IllegalArgumentException(ERR_INVALID_LOGIN_CREDENTIALS);
        }

        // 3. JWT und Refresh-Token generieren.
        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        // 4. Tokenwerte in der Datenbank speichern
        user.setRefreshTokenHash(passwordEncoder.encode(refreshToken)); // Salt im Hash enthalten, daher aber nur durch .matches() überprüfbar
        userRepository.save(user);

        // 5. AuthResponseDTO zurückgeben.
        return new AuthResponseDTO(accessToken, refreshToken, user.getId(), username);
    }

    /**
     * Erneuert den Access-Token (das JWT) basierend auf der Gültigkeit des bereitgestellten Refresh-Tokens.
     * Wenn das Refresh-Token gültig ist, wird ein neues JWT generiert und zurückgegeben.
     * <p>
     * WICHTIG: Das Refresh-Token selbst wird nicht erneuert oder geändert.
     * Es bleibt bis zu seinem ursprünglichen Ablaufdatum gültig.
     *
     * @param refreshTokenDTO Das DTO, das das Refresh-Token enthält.
     * @return AuthResponseDTO mit dem neuen Access-Token.
     */
    public AuthResponseDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Ungültiges Refresh-Token.");
        }

        UUID userId = jwtProvider.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        String storedRefreshTokenHash = user.getRefreshTokenHash();
        if (storedRefreshTokenHash == null ||
                !passwordEncoder.matches(refreshToken, storedRefreshTokenHash)) {
            throw new IllegalArgumentException("Refresh-Token stimmt nicht überein.");
        }

        String newAccessToken = jwtProvider.createAccessToken(user);

        return new AuthResponseDTO(
                newAccessToken,
                refreshToken,
                user.getId(),
                user.getUsername()
        );
    }
}
package de.pse.oys.service;

import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;
import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.auth.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service-Klasse für die Verwaltung von Benutzerkonten.
 * Diese Klasse kapselt die Logik für die Registrierung, Validierung und
 * Löschung von Nutzern unter Verwendung von JWT und Passwort-Verschlüsselung.
 * @author utgid
 * @version 1.0
 */
@Service
public class UserService {

    // --- Konstanten für Fehlermeldungen ---
    private static final String ERR_USER_NOT_FOUND = "Nutzer mit der ID %s wurde nicht gefunden.";
    private static final String ERR_USERNAME_TAKEN = "Der Benutzername ist bereits vergeben.";
    private static final String ERROR_USERNAME_TOO_SHORT = "Username zu kurz.";
    private static final String ERROR_PASSWORD_TOO_SHORT = "Passwort zu kurz.";

    // --- Konstanten für Validierungswerte ---
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * Erzeugt eine neue Instanz des UserService.
     *
     * @param userRepository  Das Repository für den Datenbankzugriff auf Nutzer.
     * @param passwordEncoder Die Komponente zur sicheren Verschlüsselung von Passwörtern.
     * @param jwtProvider     Die Komponente zur Erstellung und Validierung von JSON Web Tokens.
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Registriert einen neuen lokalen Nutzer.
     * Nutzt die spezifische Klasse {@link LocalUser}.
     * @param dto Die Registrierungsdaten des neuen Nutzers.
     * @return Die Authentifizierungsantwort mit Tokens und Nutzerinformationen.
     */
    @Transactional
    public AuthResponseDTO register(LoginDTO dto) throws IllegalArgumentException{
        validateRegistration(dto);

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException(ERR_USERNAME_TAKEN);
        }

        // Passwort verschlüsseln
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // Bei BCrypt ist das Salt im Hash enthalten, daher übergeben wir nur den Hash.
        LocalUser newUser = new LocalUser(dto.getUsername(), encodedPassword);

        User savedUser = userRepository.save(newUser);

        String accessToken = jwtProvider.createAccessToken(savedUser);
        String refreshToken = jwtProvider.createRefreshToken(savedUser);

        newUser.setRefreshTokenHash(passwordEncoder.encode(refreshToken));

        return new AuthResponseDTO(accessToken, refreshToken);
    }

    /**
     * Löscht das Konto eines Nutzers dauerhaft aus dem System.
     *
     * @param userId Die ID des zu löschenden Nutzers.
     */
    @Transactional
    public void deleteAccount(UUID userId) throws IllegalArgumentException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERR_USER_NOT_FOUND, userId)));

        userRepository.delete(user);
    }

    /**
     * Validiert die übergebenen Registrierungsdaten auf fachliche Korrektheit.
     *
     * @param dto Die zu prüfenden Daten.
     */
    private void validateRegistration(LoginDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().length() <= MIN_USERNAME_LENGTH) {
            throw new IllegalArgumentException(ERROR_USERNAME_TOO_SHORT);
        }
        if (dto.getPassword() == null || dto.getPassword().length() <= MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(ERROR_PASSWORD_TOO_SHORT);
        }
    }
}
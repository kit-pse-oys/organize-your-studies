package de.pse.oys.controller;

import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.service.UserService;
import de.pse.oys.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-Controller für die Verwaltung von Benutzerprofilen.
 * Ermöglicht die Registrierung neuer lokaler Nutzer sowie die Löschung von Konten.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController{

    private final UserService userService;
    private final AuthService authService;

    /**
     * Erzeugt eine neue Instanz des UserControllers.
     * @param userService Der Service für die Benutzerverwaltung.
     * @param authService Der Service für die Authentifizierung.
     */
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Registriert einen neuen Benutzer im System.
     * @param dto Die Registrierungsdaten des Nutzers.
     * @return Eine ResponseEntity mit den Authentifizierungsdaten (Status 201).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody LoginDTO dto) {
        AuthResponseDTO response;
        if (dto.getAuthType() == AuthType.BASIC) {
            // Lokale Registrierung über UserService
            response = userService.register(dto);
        } else {
            // OIDC-Registrierung (Just-in-Time) über AuthService
            // Es wird hier die Login-Methode, da sie bereits alles Nötige tut
            response = authService.login(dto);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

        /**
        * Authentifiziert einen Benutzer mit den angegebenen Anmeldedaten.
        * @param dto Die Anmeldedaten des Nutzers.
        * @return Eine ResponseEntity mit den Authentifizierungsdaten (Status 200).
        */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }


    /**
     * Erneuert den Access-Token unter Verwendung eines gültigen Refresh-Tokens.
     * @param dto Das DTO, welches den Refresh-Token enthält.
     * @return Eine ResponseEntity mit dem neuen Access-Token (Status 200).
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody RefreshTokenDTO dto) {
        AuthResponseDTO response = authService.refreshToken(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Löscht das Konto des aktuell authentifizierten Nutzers permanent.
     *
     * @return Status 204 (No Content) bei Erfolg oder 403 (Forbidden) bei falscher Verifizierung.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {
        UUID userId = getAuthenticatedUserId();
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
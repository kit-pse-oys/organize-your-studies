package de.pse.oys.controller;

import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für die Identitäts- und Zugriffskontrolle (JWT).
 * Stellt Endpunkte für Login, Logout und Token-Refresh bereit.
 * @author utgid
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Erzeugt eine neue Instanz des AuthControllers.
     * @param authService Der Service für die Benutzer- und Tokenverwaltung.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Verarbeitet Anmeldedaten und stellt bei Erfolg JWT-Tokens aus.
     * @param dto Die Anmeldedaten (Username/Passwort oder Provider-Daten).
     * @return Eine ResponseEntity mit den neuen Tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO dto) {

        try {
            AuthResponseDTO response = authService.login(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Beendet die Sitzung und invalidiert den Refresh-Token in der Datenbank.
     * @param dto Enthält den zu invalidierenden Refresh-Token.
     * @return ResponseEntity mit Status 204 (No Content) bei Erfolg.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody AuthResponseDTO dto) {
        // Logik zur Invalidierung des Refresh-Tokens
        return ResponseEntity.noContent().build();
    }

    /**
     * Erneuert den Access-Token unter Verwendung eines gültigen Refresh-Tokens.
     * @param dto Das DTO, welches den Refresh-Token enthält.
     * @return Eine ResponseEntity mit dem neuen Access-Token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody RefreshTokenDTO dto) {

        try{
            AuthResponseDTO response = authService.refreshToken(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
    }
}
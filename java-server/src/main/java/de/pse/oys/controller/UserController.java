package de.pse.oys.controller;

import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.service.UserService;
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

    /**
     * Erzeugt eine neue Instanz des UserControllers.
     * @param userService Der Service für die Benutzerverwaltung.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registriert einen neuen Benutzer im System.
     * @param dto Die Registrierungsdaten des Nutzers.
     * @return Eine ResponseEntity mit den Authentifizierungsdaten (Status 201).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody LoginDTO dto) {
        AuthResponseDTO response = userService.register(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

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
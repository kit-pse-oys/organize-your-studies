package de.pse.oys.controller;

import de.pse.oys.dto.UserDTO;
import de.pse.oys.dto.auth.AuthResponseDTO;
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
 * Ermöglicht die Registrierung neuer Nutzer sowie die Löschung von Konten.
 */
@RestController
@RequestMapping("/users")
public class UserController extends BaseController{
    private static final String ERROR_MSG_INTERNAL_SERVER = "Ein Fehler ist aufgetreten.";

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
    public ResponseEntity<?> register(@RequestBody UserDTO dto) {
        try {
            AuthResponseDTO response = userService.register(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            // Und für alle anderen Fehler noch einen Catch...
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MSG_INTERNAL_SERVER);
        }
    }

    /**
     * Löscht das Konto des aktuell authentifizierten Nutzers.
     * @param dto Zusätzliche Daten zur Verifizierung der Löschung (z. B. Passwort).
     * @return Eine leere ResponseEntity (Status 204).
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@RequestBody UserDTO dto) {
        UUID userId = getAuthenticatedUserId();

        userService.deleteAccount(userId, dto);
        return ResponseEntity.noContent().build();
    }
}
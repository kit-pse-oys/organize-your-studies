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
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserDTO dto) { //todo user dto ist quatsch, login dto ist besser
        try {
            AuthResponseDTO response = userService.register(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            // Und für alle anderen Fehler noch einen Catch...
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Löscht das Konto des aktuell authentifizierten Nutzers permanent.
     * Verifiziert die Anfrage anhand der im DTO bereitgestellten Daten.
     *
     * @param dto Zusätzliche Daten zur Verifizierung der Löschung (z. B. Passwort).
     * @return Status 204 (No Content) bei Erfolg oder 403 (Forbidden) bei falscher Verifizierung.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@RequestBody UserDTO dto) { //todo kein dto benötigt, da user eh schon authentifiziert ist
        try {
            UUID userId = getAuthenticatedUserId();

            // Der Service prüft die Berechtigung (z.B. Passwortabgleich)
            userService.deleteAccount(userId, dto);

            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            // Falls das Passwort falsch war oder die Berechtigung fehlt
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            // Falls der User nicht existiert oder das DTO fehlerhaft ist
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Unerwarteter Serverfehler
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
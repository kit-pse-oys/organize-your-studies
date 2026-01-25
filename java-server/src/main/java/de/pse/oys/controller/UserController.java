package de.pse.oys.controller;

import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.UserDTO;
import de.pse.oys.security.UserPrincipal;
import de.pse.oys.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-Controller für die Verwaltung von Benutzerprofilen.
 * Ermöglicht die Registrierung neuer Nutzer sowie die Löschung von Konten.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

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
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserDTO dto) {
        AuthResponseDTO response = userService.register(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Löscht das Konto des aktuell authentifizierten Nutzers.
     * @param dto Zusätzliche Daten zur Verifizierung der Löschung (z. B. Passwort).
     * @return Eine leere ResponseEntity (Status 204).
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@RequestBody UserDTO dto) {

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        UUID userId = principal.getUserId();

        userService.deleteAccount(userId, dto);
        return ResponseEntity.noContent().build();
    }
}
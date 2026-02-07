package de.pse.oys.controller;

import de.pse.oys.dto.InvalidDtoException;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Zentraler Exception-Handler für alle Controller.
 * Fängt fachliche Exceptions ab und wandelt sie in standardisierte HTTP-Antworten um.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MSG_FORBIDDEN = "Zugriff verweigert: ";
    private static final String MSG_INTERNAL_ERROR = "Ein unerwarteter interner Fehler ist aufgetreten:";
    /**
     * Behandelt Fälle, in denen Ressourcen nicht existieren (404 Not Found).
     */
    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<String> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Behandelt Berechtigungsfehler (403 Forbidden).
     */
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<String> handleForbidden(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MSG_FORBIDDEN+ ex.getMessage());
    }

    /**
     * Behandelt fehlerhafte Client-Anfragen oder Validierungsfehler (400 Bad Request).
     */
    @ExceptionHandler({
            ValidationException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            InvalidDtoException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Globaler Catch-All für unerwartete Serverfehler (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MSG_INTERNAL_ERROR + ex.getMessage());
    }
}
package de.pse.oys.service.exception;

/**
 * Exception, die geworfen wird, wenn eine Validierung fehlschlägt.
 */
public class ValidationException extends RuntimeException {

    /**
     * Erstellt eine neue ValidationException mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Validierungsfehlers
     */
    public ValidationException(String message) {
        super(message);
    }
}
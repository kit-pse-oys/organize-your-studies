package de.pse.oys.service.auth;

/**
 * InvalidTokenException – Ausnahme, die ausgelöst wird, wenn ein ungültiges Token erkannt wird.
 *
 * @author uhupo
 * @version 1.0
 */
public class InvalidTokenException extends RuntimeException {
    /**
     * Konstruktor für InvalidTokenException.
     * @param message Die Fehlermeldung, die die Ausnahme beschreibt.
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}

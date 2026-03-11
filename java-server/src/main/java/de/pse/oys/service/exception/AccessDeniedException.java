package de.pse.oys.service.exception;

/**
 * Exception, die geworfen wird, wenn ein Zugriff auf eine Ressource oder Aktion nicht erlaubt ist.
 * @author uhupo
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Erstellt eine neue AccessDeniedException mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Zugriffsfehlers
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
package de.pse.oys.service.exception;

/**
 * Exception, die geworfen wird, wenn eine angeforderte Ressource nicht gefunden wurde.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Erstellt eine neue ResourceNotFoundException mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Fehlers
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
package de.pse.oys.service.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super("Zugriff verweigert.");
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}

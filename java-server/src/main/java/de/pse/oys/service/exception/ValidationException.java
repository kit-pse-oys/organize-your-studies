package de.pse.oys.service.exception;

public class ValidationException extends RuntimeException {

    public ValidationException() {
        super("Validierung fehlgeschlagen.");
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package de.pse.oys.service.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("Nutzer ist nicht eingeloggt oder Token ist abgelaufen.");
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

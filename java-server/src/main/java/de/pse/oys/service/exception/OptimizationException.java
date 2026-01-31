package de.pse.oys.service.exception;

public class OptimizationException extends RuntimeException {
    public OptimizationException() {
        super("Optimization failed.");
    }

    public OptimizationException(String message) {
        super(message);
    }

    public OptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
}

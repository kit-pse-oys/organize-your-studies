package de.pse.oys.dto;

/**
 * InvalidDtoException – Ausnahme für ungültige DTOs.
 * Wird in erster Linie verwendet, um fehlerhafte Datenübertragungsobjekte zu kennzeichnen, sobald diese erkannt werden.
 * Das passiert wenn jackson zwar den Body in ein DTO mappen konnte,
 * die Daten aber nicht den Validierungsregeln entsprechen.
 * Bsp. fehlende Pflichtfelder oder ungültige Werte.
 *
 * @author uhupo
 * @version 1.0
 */
public class InvalidDtoException extends RuntimeException {
    /**
     * Konstruktor für InvalidDtoException.
     * @param message Fehlermeldung
     */
    public InvalidDtoException(String message) {
        super(message);
    }
}

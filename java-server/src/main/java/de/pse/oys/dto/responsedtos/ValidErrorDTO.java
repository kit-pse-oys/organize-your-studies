package de.pse.oys.dto.responsedtos;

import java.time.LocalDateTime;

/**
 * Datentransferobjekt für die Rückgabe von Validierungsfehlern an das Frontend.
 * Bietet eine einheitliche Struktur für Fehlermeldungen inklusive Zeitstempel.
 * @author utgid
 * @version 1.0
 */
public class ValidErrorDTO {

    /** Der Zeitpunkt, an dem der Fehler aufgetreten ist. */
    private LocalDateTime timestamp;

    /** Die detaillierte Fehlermeldung für den Nutzer oder Entwickler. */
    private String message;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public ValidErrorDTO() {
    }

    /**
     * Erzeugt ein neues Fehler-DTO mit den angegebenen Informationen.
     * * @param timestamp Zeitpunkt des Fehlers.
     * @param message   Inhalt der Fehlermeldung.
     */
    public ValidErrorDTO(LocalDateTime timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    // Getter

    /** @return Der Zeitstempel des Fehlers. */
    public LocalDateTime getTimestamp() { return timestamp; }

    /** @return Die Fehlermeldung. */
    public String getMessage() { return message; }

    // Setter

    /** @param timestamp Der zu setzende Zeitpunkt. */
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /** @param message Die zu setzende Fehlermeldung. */
    public void setMessage(String message) { this.message = message; }
}
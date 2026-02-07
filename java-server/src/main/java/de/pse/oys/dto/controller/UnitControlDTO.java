package de.pse.oys.dto.controller;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Container für Anfragen zur Manipulation von Lerneinheiten.
 * Bildet die verschiedenen POST-Operationen des Clients ab.
 */
public class UnitControlDTO {
    private UUID id;
    private LocalDateTime newTime;
    private Integer actualDuration;


    // Getter und Setter

    /**
     * Gibt die eindeutige ID zurück.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Setzt die eindeutige ID.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gibt den neu gesetzten Zeitpunkt zurück.
     */
    public LocalDateTime getNewTime() {
        return newTime;
    }

    /**
     * Setzt den neuen Zeitpunkt.
     */
    public void setNewTime(LocalDateTime newTime) {
        this.newTime = newTime;
    }

    /** Gibt die tatsächliche Dauer zurück. */
    public Integer getActualDuration() { return actualDuration; }

    /** Setzt die tatsächliche Dauer. */
    public void setActualDuration(Integer actualDuration) { this.actualDuration = actualDuration; }
}
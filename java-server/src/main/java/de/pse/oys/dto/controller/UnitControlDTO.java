package de.pse.oys.dto.controller;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Container für Anfragen zur Manipulation von Lerneinheiten.
 * Bildet die verschiedenen POST-Operationen des Clients ab.
 */
public class UnitControlDTO {
    private UUID id;
    private Boolean finished;
    private Boolean automaticNewTime;
    private LocalDateTime newTime;


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
     * Gibt zurück, ob der Vorgang abgeschlossen ist.
     */
    public Boolean getFinished() {
        return finished;
    }

    /**
     * Setzt den Abschlussstatus des Vorgangs.
     */
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    /**
     * Gibt zurück, ob die neue Zeit automatisch gesetzt wird.
     */
    public Boolean getAutomaticNewTime() {
        return automaticNewTime;
    }

    /**
     * Setzt, ob die neue Zeit automatisch gesetzt wird.
     */
    public void setAutomaticNewTime(Boolean automaticNewTime) {
        this.automaticNewTime = automaticNewTime;
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
}
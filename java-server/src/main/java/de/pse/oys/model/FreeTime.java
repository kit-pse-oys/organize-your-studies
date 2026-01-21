package de.pse.oys.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Repräsentiert einen Zeitraum, in dem der Nutzer keine Lerneinheiten einplanen möchte.
 * Diese Freizeitblöcke werden vom Planungsalgorithmus als harte Restriktionen behandelt.
 */
@Entity
@Table(name = "free_times")
public class FreeTime {

    /** Eindeutige Kennung des Freizeitblocks (readOnly). */
    @Id
    @Column(name = "free_time_id", updatable = false)
    private UUID freeTimeId;

    /** Kurze Beschreibung oder Name der Freizeitaktivität. */
    @Column(name = "title", nullable = false)
    private String title;

    /** Der Startzeitpunkt des Freizeitblocks. */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Der Endzeitpunkt des Freizeitblocks. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Gibt den Typ der Wiederholung zurück.
     * Das Feld wird über die Unterklassen gesteuert.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type_discriminator", insertable = false, updatable = false)
    private RecurrenceType recurrenceType;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected FreeTime() {
    }

    /**
     * Erzeugt einen neuen Freizeitblock.
     *
     * @param freeTimeId  Eindeutige ID.
     * @param title Name/Beschreibung (z. B. "Fußballtraining").
     * @param startTime   Beginn der Freizeit.
     * @param endTime     Ende der Freizeit.
     */
    public FreeTime(UUID freeTimeId, String title, LocalTime startTime, LocalTime endTime, RecurrenceType type) {
        this.freeTimeId = freeTimeId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrenceType = type;
    }

    /**
     * Berechnet die Dauer des Freizeitblocks in Minuten.
     * @return Dauer in Minuten.
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Prüft, ob der Zeitraum logisch konsistent ist (Start vor Ende).
     * @return true, wenn Startzeit chronologisch vor Endzeit liegt.
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    // Getter

    /** @return Die ID des Freizeitblocks. */
    public UUID getFreeTimeId() {
        return freeTimeId;
    }

    /** @return Die Beschreibung der Freizeit. */
    public String getTitle() {
        return title;
    }

    /** @return Den Startzeitpunkt. */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** @return Den Endzeitpunkt. */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * @return Der spezifische Wiederholungstyp dieser Freizeitinstanz.
     */
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    // Setter

    /** @param title Die neue Beschreibung. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @param startTime Der neue Startzeitpunkt. */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /** @param endTime Der neue Endzeitpunkt. */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
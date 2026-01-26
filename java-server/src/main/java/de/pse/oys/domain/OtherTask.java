package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Repräsentiert eine allgemeine Aufgabe ohne punktuelle Deadline.
 * Diese Aufgabe ist stattdessen an einen definierten Bearbeitungszeitraum gebunden.
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("OTHER")
public class OtherTask extends Task {

    /** Der Beginn des Zeitfensters für die Bearbeitung. */
    @Column(name = "time_frame_start")
    private LocalDateTime startTime;

    /** Das Ende des Zeitfensters für die Bearbeitung. */
    @Column(name = "time_frame_end")
    private LocalDateTime endTime;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected OtherTask() {
        super();
    }

    /**
     * Erzeugt eine neue allgemeine Aufgabe.
     *
     * @param title                 Titel der Aufgabe.
     * @param weeklyDurationMinutes Wöchentlicher Aufwand in Minuten.
     * @param startTime             Beginn des Zeitraums.
     * @param endTime               Ende des Zeitraums.
     */
    public OtherTask(String title, int weeklyDurationMinutes, LocalDateTime startTime, LocalDateTime endTime) {
        super(title, weeklyDurationMinutes, TaskCategory.OTHER);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Gibt das Ende des Bearbeitungszeitraums als harte Zeitgrenze zurück.
     * @return Das Ende des Zeitrahmens.
     */
    @Override
    public LocalDateTime getHardDeadline() {
        return endTime;
    }

    // Getter & Setter

    /** @return Den Start des Zeitrahmens. */
    public LocalDateTime getStartTime() { return startTime; }

    /** @return Das Ende des Zeitrahmens. */
    public LocalDateTime getEndTime() { return endTime; }

    /** @param startTime Der neue Startzeitpunkt. */
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /** @param endTime Der neue Endzeitpunkt. */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
package de.pse.oys.domain;

import de.pse.oys.domain.enums.UnitStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

/**
 * Repräsentiert eine konkrete Lerneinheit innerhalb eines Lernplans.
 * Eine Lerneinheit ist einer spezifischen Aufgabe zugeordnet und besitzt
 * einen geplanten sowie einen tatsächlichen Zeitrahmen.
 */
@Entity
@Table(name = "learning_units")
public class LearningUnit {

    /** Eindeutige Kennung der Lerneinheit. */
    @Id
    @Column(name = "unitid", updatable = false)
    private UUID unitId;

    /** Der geplante Startzeitpunkt der Lerneinheit. */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** Das geplante Ende der Lerneinheit. */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** Die tatsächlich für diese Einheit aufgewendete Zeit in Minuten. */
    @Column(name = "actual_duration_minutes")
    private int actualDurationMinutes;

    /** Der aktuelle Status der Ausführung (geplant, abgeschlossen, verpasst). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UnitStatus status;

    /** Die Aufgabe, die in dieser Einheit bearbeitet wird. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taskid", nullable = false, updatable = false)
    private Task task;

    /** Die vom Nutzer abgegebene Bewertung nach Abschluss der Einheit. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ratingid")
    private UnitRating rating;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected LearningUnit() {
    }

    /**
     * Erzeugt eine neue geplante Lerneinheit.
     *
     * @param unitId    Eindeutige ID der Einheit.
     * @param task      Die zugeordnete Aufgabe.
     * @param startTime Geplanter Beginn.
     * @param endTime   Geplantes Ende.
     */
    public LearningUnit(UUID unitId, Task task, LocalDateTime startTime, LocalDateTime endTime) {
        this.unitId = unitId;
        this.task = task;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = UnitStatus.PLANNED;
    }

    /**
     * Markiert die Einheit als abgeschlossen und speichert die tatsächliche Dauer.
     *
     * @param actualMinutes Die tatsächlich aufgewendete Zeit in Minuten.
     */
    public void markAsCompleted(int actualMinutes) {
        this.actualDurationMinutes = actualMinutes;
        this.status = UnitStatus.COMPLETED;
    }

    /**
     * Prüft, ob sich diese Lerneinheit zeitlich mit einer anderen Einheit überschneidet.
     * Eine Überschneidung liegt vor, wenn der Start dieser Einheit vor dem Ende der anderen
     * liegt UND das Ende dieser Einheit nach dem Start der anderen liegt.
     *
     * @param other Die andere Lerneinheit, die geprüft werden soll.
     * @return true, wenn eine zeitliche Überschneidung vorliegt.
     */
    public boolean isOverlapping(LearningUnit other) {
        if (other == null) return false;
        return this.startTime.isBefore(other.getEndTime()) &&
                this.endTime.isAfter(other.getStartTime());
    }

    /**
     * Berechnet die ursprünglich geplante Dauer der Einheit in Minuten.
     *
     * @return Die Differenz zwischen Start- und Endzeitpunkt in Minuten.
     */
    public long getPlannedDuration() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Gibt an, ob für diese Lerneinheit bereits eine Bewertung durch den Nutzer vorliegt.
     *
     * @return true, wenn ein UnitRating-Objekt verknüpft ist.
     */
    public boolean isRated() {
        return this.rating != null;
    }


    // Getter

    /** @return Die ID der Lerneinheit. */
    public UUID getUnitId() { return unitId; }

    /** @return Der geplante Startzeitpunkt. */
    public LocalDateTime getStartTime() { return startTime; }

    /** @return Der geplante Endzeitpunkt. */
    public LocalDateTime getEndTime() { return endTime; }

    /** @return Die tatsächliche Bearbeitungsdauer in Minuten. */
    public int getActualDurationMinutes() { return actualDurationMinutes; }

    /** @return Der aktuelle Bearbeitungsstatus. */
    public UnitStatus getStatus() { return status; }

    /** @return Die verknüpfte Aufgabe. */
    public Task getTask() { return task; }

    /** @return Die Bewertung der Einheit, falls vorhanden. */
    public UnitRating getRating() { return rating; }

    // Setter

    /** @param startTime Der neue Startzeitpunkt. */
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /** @param endTime Der neue Endzeitpunkt. */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /** @param status Der neue Status der Einheit. */
    public void setStatus(UnitStatus status) { this.status = status; }

    /** @param rating Die Bewertung der Einheit nach der Durchführung. */
    public void setRating(UnitRating rating) { this.rating = rating; }
}
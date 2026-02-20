package de.pse.oys.domain;

import de.pse.oys.domain.enums.UnitStatus;
import de.pse.oys.dto.UnitDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

/**
 * Repräsentiert eine konkrete Lerneinheit innerhalb eines Lernplans.
 * Eine Lerneinheit ist einer spezifischen Aufgabe zugeordnet und besitzt
 * einen geplanten sowie einen tatsächlichen Zeitrahmen.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "learning_units")
public class LearningUnit {

    /** Eindeutige Kennung der Lerneinheit. */
    @Id
    @GeneratedValue
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
     * @param task      Die zugeordnete Aufgabe.
     * @param startTime Geplanter Beginn.
     * @param endTime   Geplantes Ende.
     */
    public LearningUnit(Task task, LocalDateTime startTime, LocalDateTime endTime) {
        this.task = task;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = UnitStatus.PLANNED;
    }

    /**
     * Markiert die Einheit als abgeschlossen und speichert die tatsächliche Dauer.
     * Wird aufgerufen, wenn der Nutzer die Einheit frühzeitig bendet hat.
     * @param actualMinutes Die tatsächlich aufgewendete Zeit in Minuten.
     */
    public void markAsCompletedEarly(int actualMinutes) {
        this.actualDurationMinutes = actualMinutes;
        this.status = UnitStatus.COMPLETED;
    }

    /**
     * Markiert die Lerneinheit als abgeschlossen.
     * Der Status wird auf COMPLETED gesetzt und die geplante Dauer bleibt unverändert.
     */
    public void markAsCompleted() {
        this.status = UnitStatus.COMPLETED;
        this.actualDurationMinutes = (int) Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Markiert die Lerneinheit als verpasst.
     */
    public void markAsMissed() {
        this.status = UnitStatus.MISSED;
    }

    /**
     * Prüft, ob die Lerneinheit bereits zeitlich abgeschlossen ist.
     * Eine Einheit gilt als vergangen, wenn ihr Endzeitpunkt vor der aktuellen Zeit liegt.
     *
     * @return true, wenn die Einheit in der Vergangenheit liegt.
     */
    public boolean hasPassed() {
        if (this.endTime == null) {
            return false;
        }
        boolean isPast = java.time.LocalDateTime.now().isAfter(this.endTime);
        if (this.status == UnitStatus.PLANNED && isPast) {
            this.status = UnitStatus.COMPLETED;
        }
        return isPast;
    }

    /**
     * Gibt an, ob für diese Lerneinheit bereits eine Bewertung durch den Nutzer vorliegt.
     *
     * @return true, wenn ein UnitRating-Objekt verknüpft ist.
     */
    public boolean isRated() {
        return this.rating != null;
    }


    /** Erstellt ein UnitDTO aus einer LearningUnit.
     * @return das DTO mit den relevanten Informationen dieser Einheit.
     */
    public UnitDTO toDTO() {
        UnitDTO dto = new UnitDTO();

        if (task != null) {
            dto.setTask(task.getTaskId());
        }
        if (startTime != null) {
            dto.setDate(startTime.toLocalDate());
            dto.setStart(startTime.toLocalTime());
        }

        if (endTime != null) {
            dto.setEnd(endTime.toLocalTime());
            if (dto.getDate() == null) {
                dto.setDate(endTime.toLocalDate());
            }
        }
        return dto;
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

    /**
     * Setzt die Bewertung der Lerneinheit und markiert sie als abgeschlossen.
     * @param rating Die Bewertung der Einheit nach der Durchführung.
     */
    public void setRating(UnitRating rating) {
        this.rating = rating;
    }
}
package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repräsentiert eine Aufgabe mit einem festen Abgabetermin (Deadline).
 * Diese Klasse wird für zeitkritische Abgaben wie Hausarbeiten genutzt.
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("SUBMISSION")
public class SubmissionTask extends Task {

    /** Der präzise Zeitpunkt der Abgabefrist. */
    @Column(name = "fixed_deadline")
    private LocalDateTime deadline;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected SubmissionTask() {
        super();
    }

    /**
     * Erzeugt eine neue Aufgabe mit Abgabefrist.
     *
     * @param title                 Titel der Aufgabe.
     * @param weeklyDurationMinutes Wöchentlicher Aufwand in Minuten.
     * @param deadline              Der feste Abgabetermin.
     */
    public SubmissionTask(String title, int weeklyDurationMinutes, LocalDateTime deadline) {
        super(title, weeklyDurationMinutes, TaskCategory.SUBMISSION);
        this.deadline = deadline;
    }

    /**
     * Gibt die festgelegte Deadline als harten Endpunkt für die Planung zurück.
     * @return Die Abgabefrist.
     */
    @Override
    public LocalDateTime getHardDeadline() {
        return deadline;
    }

    @Override
    protected boolean isActive() {
        return deadline != null && LocalDateTime.now().isBefore(deadline);
    }

    // Getter & Setter

    /** @return Die aktuelle Deadline. */
    public LocalDateTime getDeadline() {
        return deadline;
    }

    /** @param deadline Die neue Deadline. */
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
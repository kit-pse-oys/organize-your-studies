package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Repräsentiert eine Aufgabe mit wiederkehrenden Abgaben.
 *
 * Konzept:
 * - firstDeadline: "allererste Deadline" (Anchor)
 * - cycleWeeks: alle x Wochen wiederholt sich die Deadline
 * - endTime: ab dann ist die Aufgabe komplett vorbei (keine Deadlines mehr)
 *
 * @version 1.1
 * @author uqvfm
 */
@Entity
@DiscriminatorValue("SUBMISSION")
public class SubmissionTask extends Task {

    /** Zeitpunkt, ab dem die Aufgabe komplett vorbei ist (keine Deadlines mehr). */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** Erste Deadline (Anchor), an der sich die Wiederholung ausrichtet. */
    @Column(name = "first_deadline", nullable = false)
    private LocalDateTime firstDeadline;

    /** Rhythmus in Wochen (1 = wöchentlich, 2 = alle 2 Wochen, ...). */
    @Column(name = "cycle_weeks", nullable = false)
    private int cycleWeeks;

    /** Standardkonstruktor für JPA/Hibernate. */
    protected SubmissionTask() {
        super();
    }

    /**
     * Erzeugt eine SubmissionTask mit wiederkehrenden Deadlines bis endTime.
     */
    public SubmissionTask(
            String title,
            int weeklyDurationMinutes,
            LocalDateTime firstDeadline,
            int cycleWeeks,
            LocalDateTime endTime
    ) {
        super(title, weeklyDurationMinutes, TaskCategory.SUBMISSION);
        this.firstDeadline = firstDeadline;
        this.cycleWeeks = Math.max(1, cycleWeeks);
        this.endTime = endTime;
    }

    /**
     * Für die Planung ist die "harte Deadline" die nächste anstehende Abgabe (>= jetzt).
     * Gibt null zurück, wenn es keine weiteren Deadlines mehr gibt (z.B. nach endTime).
     */
    @Override
    public LocalDateTime getHardDeadline() {
        return computeNextDeadline(LocalDateTime.now());
    }

    /**
     * Bestimmt, ob die Aufgabe fachlich noch aktiv ist.
     *
     * @return {@code true}, wenn {@code endTime} gesetzt ist und der aktuelle Zeitpunkt davor liegt,
     * sonst {@code false}.
     */
    @Override
    public boolean isActive() {
        return endTime != null && LocalDateTime.now().isBefore(endTime);
    }

    // ===== Business logic =====

    private LocalDateTime computeNextDeadline(LocalDateTime from) {
        if (firstDeadline == null || endTime == null) {
            return null;
        }

        // Wenn wir schon nach der endTime sind -> keine Deadlines mehr
        if (!from.isBefore(endTime)) {
            return null;
        }

        // Wenn wir noch vor der ersten Deadline sind -> die erste ist die nächste
        if (!from.isAfter(firstDeadline)) {
            return firstDeadline.isAfter(endTime) ? null : firstDeadline;
        }

        // Von firstDeadline aus in cycleWeeks-Schritten nach vorne springen
        long weeksBetween = ChronoUnit.WEEKS.between(firstDeadline, from);
        long steps = (weeksBetween / cycleWeeks) + 1;
        LocalDateTime candidate = firstDeadline.plusWeeks(steps * (long) cycleWeeks);

        return candidate.isAfter(endTime) ? null : candidate;
    }

    // ===== Getter & Setter =====

    /**
     * @return Zeitpunkt, ab dem die Aufgabe endet (keine Deadlines mehr).
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Setzt den Endzeitpunkt der Aufgabe.
     * @param endTime Endzeitpunkt (darf nicht {@code null} sein, wenn die Aufgabe aktiv sein soll).
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * @return Erste Deadline (Anchor) der Aufgabe.
     */
    public LocalDateTime getFirstDeadline() {
        return firstDeadline;
    }

    /**
     * Setzt die erste Deadline (Anchor) der Aufgabe.
     * @param firstDeadline Erste Deadline.
     */
    public void setFirstDeadline(LocalDateTime firstDeadline) {
        this.firstDeadline = firstDeadline;
    }

    /**
     * @return Wiederholungsrhythmus in Wochen (mindestens 1).
     */
    public int getCycleWeeks() {
        return cycleWeeks;
    }

    /**
     * Setzt den Wiederholungsrhythmus in Wochen.
     * Werte kleiner als 1 werden auf 1 normalisiert.
     *
     * @param cycleWeeks Wiederholungsrhythmus (min. 1).
     */
    public void setCycleWeeks(int cycleWeeks) {
        this.cycleWeeks = Math.max(1, cycleWeeks);
    }
}
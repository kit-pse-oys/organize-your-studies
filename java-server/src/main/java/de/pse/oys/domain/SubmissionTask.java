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
 */
@Entity
@DiscriminatorValue("SUBMISSION")
public class SubmissionTask extends Task {

    /** Zeitpunkt, ab dem die Aufgabe komplett vorbei ist (keine Deadlines mehr). */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** Erste Deadline (Anchor), an der sich die Wiederholung ausrichtet. */
    @Column(name = "first_deadline")
    private LocalDateTime firstDeadline;

    /** Rhythmus in Wochen (1 = wöchentlich, 2 = alle 2 Wochen, ...). */
    @Column(name = "cycle_weeks")
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

    @Override
    protected boolean isActive() {
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

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getFirstDeadline() {
        return firstDeadline;
    }

    public void setFirstDeadline(LocalDateTime firstDeadline) {
        this.firstDeadline = firstDeadline;
    }

    public int getCycleWeeks() {
        return cycleWeeks;
    }

    public void setCycleWeeks(int cycleWeeks) {
        this.cycleWeeks = Math.max(1, cycleWeeks);
    }
}

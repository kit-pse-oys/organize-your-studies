package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.LocalDate;

/**
 * Abstrakte Basisklasse für alle Aufgabentypen im System.
 * Hält die gemeinsamen Merkmale wie Titel, Aufwand und Status.
 */
@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "taskcategory")
public abstract class Task {

    @Id
    @Column(name = "taskid", updatable = false)
    private UUID taskId;

    @Column(name = "title", nullable = false)
    private String title;

    /** Wöchentlicher Aufwand in Minuten. */
    @Column(name = "weekly_duration_minutes", nullable = false)
    private int weeklyDurationMinutes;

    /** Feste Deadline aus der DB-Spalte {@code tasks.fixed_deadline} (optional). */
    @Column(name = "fixed_deadline")
    private LocalDate fixedDeadline;

    /** Start eines Zeitfensters aus {@code tasks.time_frame_start} (optional). */
    @Column(name = "time_frame_start")
    private LocalDate timeFrameStart;

    /** Ende eines Zeitfensters aus {@code tasks.time_frame_end} (optional). */
    @Column(name = "time_frame_end")
    private LocalDate timeFrameEnd;

    /**
     * Die fachliche Kategorie der Aufgabe.
     * Das Feld ist schreibgeschützt, da es über den Diskriminator gesteuert wird.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "taskcategory", insertable = false, updatable = false)
    private TaskCategory category;

    /**
     * Die Kostenmatrix der Aufgabe für den Planungsalgorithmus.
     * Wird über OneToOne abgebildet (mappedBy in der Matrix).
     */
    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private CostMatrix costMatrix;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected Task() {}

    /**
     * Erzeugt eine neue Aufgabe.
     * @param taskId                Eindeutige ID.
     * @param title                 Bezeichnung der Aufgabe.
     * @param weeklyDurationMinutes Wöchentlicher Aufwand in Minuten.
     * @param category              Fachliche Kategorie der Aufgabe.
     */
    public Task(UUID taskId, String title, int weeklyDurationMinutes, TaskCategory category) {
        this.taskId = taskId;
        this.title = title;
        this.weeklyDurationMinutes = weeklyDurationMinutes;
        this.category = category;
    }

    /**
     * Gibt die harte Deadline der Aufgabe zurück.
     * Muss von Unterklassen mit Terminbezug überschrieben werden.
     * @return Den Zeitpunkt der harten Deadline oder null, falls nicht vorhanden.
     */
    public abstract LocalDateTime getHardDeadline();

    /**
     * Berechnet die weiche Deadline unter Berücksichtigung eines Puffers.
     * @param bufferDays Die Anzahl der Tage, die als Puffer dienen.
     * @return Der Zeitpunkt der harten Deadline abzüglich der Puffertage.
     */
    public LocalDateTime getSoftDeadline(int bufferDays) {
        LocalDateTime hard = getHardDeadline();
        return (hard != null) ? hard.minusDays(bufferDays) : null;
    }

    /**
     * Prüft, ob der aktuelle Zeitpunkt bereits nach der weichen Deadline liegt.
     * @param bufferDays Die Anzahl der Tage, die als Puffer dienen.
     * @return true, wenn die weiche Deadline überschritten wurde.
     */
    public boolean isPastSoftDeadline(int bufferDays) {
        LocalDateTime soft = getSoftDeadline(bufferDays);
        return (soft != null) && LocalDateTime.now().isAfter(soft);
    }

    // Getter & Setter

    /** @return Die eindeutige ID der Aufgabe. */
    public UUID getTaskId() { return taskId; }

    /** @return Der Titel der Aufgabe. */
    public String getTitle() { return title; }

    /** @return Der wöchentliche Aufwand in Minuten. */
    public int getWeeklyDurationMinutes() { return weeklyDurationMinutes; }

    /** @return Die fachliche Kategorie der Aufgabe. */
    public TaskCategory getCategory() { return category; }

    /** @return Die Kostenmatrix dieser Aufgabe. */
    public CostMatrix getCostMatrix() {
        return costMatrix;
    }

    /** @param costMatrix Die neu zugeordnete Kostenmatrix. */
    public void setCostMatrix(CostMatrix costMatrix) {
        this.costMatrix = costMatrix;
    }

    /** @param title Der neue Titel der Aufgabe. */
    public void setTitle(String title) { this.title = title; }

    /** @param durationMinutes Der neue wöchentliche Aufwand. */
    public void setWeeklyDurationMinutes(int durationMinutes) { this.weeklyDurationMinutes = durationMinutes; }

    public void setModule(Module module) {
    }

    /** @return feste Deadline oder {@code null}. */
    public LocalDate getFixedDeadline() { return fixedDeadline; }

    /** @param fixedDeadline feste Deadline (oder {@code null}). */
    public void setFixedDeadline(LocalDate fixedDeadline) { this.fixedDeadline = fixedDeadline; }

    /** @return Startdatum des Zeitfensters oder {@code null}. */
    public LocalDate getTimeFrameStart() { return timeFrameStart; }

    /** @param timeFrameStart Startdatum (oder {@code null}). */
    public void setTimeFrameStart(LocalDate timeFrameStart) { this.timeFrameStart = timeFrameStart; }

    /** @return Enddatum des Zeitfensters oder {@code null}. */
    public LocalDate getTimeFrameEnd() { return timeFrameEnd; }

    /** @param timeFrameEnd Enddatum (oder {@code null}). */
    public void setTimeFrameEnd(LocalDate timeFrameEnd) { this.timeFrameEnd = timeFrameEnd; }

}
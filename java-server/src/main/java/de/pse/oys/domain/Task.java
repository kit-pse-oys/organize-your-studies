package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.TaskStatus;
import de.pse.oys.domain.enums.UnitStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstrakte Basisklasse für alle Aufgabentypen im System.
 * Hält die gemeinsamen Merkmale wie Titel, Aufwand und Status.
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "taskcategory")
public abstract class Task {

    @Id
    @GeneratedValue
    @Column(name = "taskid", updatable = false)
    private UUID taskId;

    @Column(name = "title", nullable = false)
    private String title;

    /** Wöchentlicher Aufwand in Minuten. */
    @Column(name = "weekly_effort_minutes", nullable = false)
    private int weeklyDurationMinutes;

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
     * Die Liste der geplanten oder abgeschlossenen Lerneinheiten für diese Aufgabe.
     * Realisiert als One-to-Many Beziehung.
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LearningUnit> learningUnits = new ArrayList<>();

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "moduleid", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Module module;

    /** Der aktuelle Status der Ausführung (offen, in arbeit, abgeschlossen). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected Task() {}

    /**
     * Erzeugt eine neue Aufgabe.
     * @param title                 Bezeichnung der Aufgabe.
     * @param weeklyDurationMinutes Wöchentlicher Aufwand in Minuten.
     * @param category              Fachliche Kategorie der Aufgabe.
     */
    public Task(String title, int weeklyDurationMinutes, TaskCategory category) {
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

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }


    /** @param costMatrix Die neu zugeordnete Kostenmatrix. */
    public void setCostMatrix(CostMatrix costMatrix) {
        this.costMatrix = costMatrix;
    }

    /** @param title Der neue Titel der Aufgabe. */
    public void setTitle(String title) { this.title = title; }

    /** @param durationMinutes Der neue wöchentliche Aufwand. */
    public void setWeeklyDurationMinutes(int durationMinutes) { this.weeklyDurationMinutes = durationMinutes; }

    /**
     * Liefert den aktuellen Ausführungsstatus der Aufgabe.
     *
     * @return Der aktuelle {@link TaskStatus} der Aufgabe.
     */
    public TaskStatus getStatus() {
        updateTaskStatus();
        return status;
    }

    /**
     * Setzt den Ausführungsstatus der Aufgabe.
     *
     * @param status Neuer {@link TaskStatus} (darf nicht null sein).
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }


    // Hilfsmethoden für learningUnits

    /**
     * Fügt der Aufgabe eine neue Lerneinheit hinzu.
     * Stellt die bidirektionale Verknüpfung sicher.
     * @param unit Die hinzuzufügende Lerneinheit.
            */
    public void addLearningUnit(LearningUnit unit) {
        if (unit != null && !this.learningUnits.contains(unit)) {
            this.learningUnits.add(unit);
        }
    }


    /**
     * @return Eine nicht veränderbare Liste aller zugehörigen Lerneinheiten.
     */
    public List<LearningUnit> getLearningUnits() {
        if (learningUnits == null) {
            learningUnits = new ArrayList<>();
        }
        return List.copyOf(learningUnits);
    }

    /**
     * Ersetzt die gesamte Liste der Lerneinheiten.
     * @param learningUnits Die neue Liste der Lerneinheiten.
     */
    public void setLearningUnits(List<LearningUnit> learningUnits) {
        this.learningUnits = learningUnits;
    }

    private void updateTaskStatus() {
        if (isActive()) {
            this.status = TaskStatus.OPEN;
        } else {
            this.status = TaskStatus.CLOSED;
        }
    }

    /** @return true, wenn die Aufgabe aktiv ist und bearbeitet werden kann. */
    protected abstract boolean isActive();
}
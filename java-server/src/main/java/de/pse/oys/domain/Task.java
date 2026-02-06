package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.UnitStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstrakte Basisklasse für alle Aufgabentypen im System.
 * Hält die gemeinsamen Merkmale wie Titel und Aufwand.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "taskcategory")
public abstract class Task {

    /** Eindeutige Kennung der Aufgabe (readOnly). */
    @Id
    @GeneratedValue
    @Column(name = "taskid", updatable = false)
    private UUID taskId;

    /** Titel/Bezeichnung der Aufgabe. */
    @Column(name = "title", nullable = false)
    private String title;

    /** Wöchentlicher Aufwand in Minuten. */
    @Column(name = "weekly_duration_minutes", nullable = false)
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

    /**
     * Zugehöriges {@link Module} der Aufgabe.
     * Wird lazy geladen; bei JSON-Serialisierung ignoriert, um Zyklen zu vermeiden.
     */
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "moduleid", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Module module;

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

    /** @return true, wenn die Aufgabe aktiv ist und bearbeitet werden kann. */
    public abstract boolean isActive();

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

    /**
     * Liefert das zugehörige Modul der Aufgabe.
     * @return Zugehöriges {@link Module}.
     */
    public Module getModule() {
        return module;
    }

    /**
     * Setzt das zugehörige Modul der Aufgabe.
     * @param module Modul, dem diese Aufgabe zugeordnet wird.
     */
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

    // helpers

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
        return List.copyOf(learningUnits);
    }

    /**
     * Ersetzt die gesamte Liste der Lerneinheiten.
     * @param learningUnits Die neue Liste der Lerneinheiten.
     */
    public void setLearningUnits(List<LearningUnit> learningUnits) {
        this.learningUnits = learningUnits;
    }
}
package de.pse.oys.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repräsentiert einen berechneten Lernplan für eine spezifische Woche.
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "learning_plans")
public class LearningPlan {

    /** Eindeutige Kennung des Plans (planid). */
    @Id
    @Column(name = "planid", updatable = false)
    private UUID planId;

    /** Beginn der Gültigkeitswoche. */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /** Ende der Gültigkeitswoche. */
    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    /**
     * Liste der im Plan enthaltenen Lerneinheiten.
     * Realisiert über die Verbindungstabelle 'plan_units'.
     */
    @ManyToMany
    @JoinTable(
            name = "plan_units",
            joinColumns = @JoinColumn(name = "planid"),
            inverseJoinColumns = @JoinColumn(name = "unitid")
    )
    private List<LearningUnit> units = new ArrayList<>();

    /**
     * Liste der berücksichtigten Freizeitblöcke für diesen Zeitraum.
     */
    @Transient // Im Diagramm S. 44 vorhanden, aber oft dynamisch zur Laufzeit berechnet
    private List<FreeTime> freeTimes = new ArrayList<>();

    /**
     * Standardkonstruktor für JPA.
     */
    protected LearningPlan() {}

    /**
     * Erzeugt einen neuen Lernplan.
     * @param id    Die UUID des Plans.
     * @param start Der Starttag der Woche.
     * @param end   Der Endtag der Woche.
     */
    public LearningPlan(UUID id, LocalDate start, LocalDate end) {
        this.planId = id;
        this.weekStart = start;
        this.weekEnd = end;
    }

    /**
     * Filtert alle Einheiten des Plans für ein bestimmtes Datum.
     * @param date Das angefragte Datum.
     * @return Liste der Einheiten, die am angefragten Tag stattfinden.
     */
    public List<LearningUnit> getUnitsForDay(LocalDate date) {
        return units.stream()
                .filter(unit -> unit.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    // Getter & Setter

    /** @return Die eindeutige Kennung des Lernplans. */
    public UUID getPlanId() { return planId; }

    /** @return Das Startdatum der Woche, für die dieser Plan gilt. */
    public LocalDate getWeekStart() { return weekStart; }

    /** @return Das Enddatum der Woche, für die dieser Plan gilt. */
    public LocalDate getWeekEnd() { return weekEnd; }

    /** @return Die Liste aller im Plan enthaltenen Lerneinheiten. */
    public List<LearningUnit> getUnits() { return units; }

    /** @return Die Liste der für diesen Plan berücksichtigten Freizeitblöcke. */
    public List<FreeTime> getFreeTimes() { return freeTimes; }

    /** @param units Die Liste der Lerneinheiten, die diesem Plan zugeordnet werden sollen. */
    public void setUnits(List<LearningUnit> units) { this.units = units; }
}
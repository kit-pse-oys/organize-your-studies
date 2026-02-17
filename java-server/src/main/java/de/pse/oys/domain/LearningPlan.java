package de.pse.oys.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repräsentiert einen berechneten Lernplan für eine spezifische Woche.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "learning_plans")
public class LearningPlan {

    /**
     * Zugehöriger Nutzer dieses Lernplans.
     * Wird über die Fremdschlüsselspalte {@code user_id} persistiert.
     * Wird nach der Erstellung nicht mehr geändert (readOnly).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    /** Eindeutige Kennung des Plans (planid). */
    @Id
    @GeneratedValue
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
    @Transient
    private List<FreeTime> freeTimes = new ArrayList<>();

    /**
     * Standardkonstruktor für JPA.
     */
    protected LearningPlan() {}

    /**
     * Erzeugt einen neuen Lernplan.
     *
     * @param start Der Starttag der Woche.
     * @param end   Der Endtag der Woche.
     */
    public LearningPlan(LocalDate start, LocalDate end) {
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

    /** @return Nutzer-ID. */
    public UUID getUserId() {
        return (user != null) ? user.getId() : null;
    }

    /**
     * @return Den zugehörigen Nutzer (lazy geladen).
     */
    public User getUser() {
        return user;
    }

    /**
     * Setzt den Nutzer dieses Lernplans.
     *
     * @param user der zu setzende Nutzer
     */
    public void setUser(User user) {
        this.user = user;
    }

    /** @param userId die UUID des Users, dem der Lernplan zugeordnet werden soll. */
    public void setUserId(UUID userId) {
    }
}

package de.pse.oys.domain;

import de.pse.oys.domain.enums.TimeSlot;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Speichert die individuellen Lernpräferenzen eines Nutzers.
 * Dazu gehören die gewünschte Dauer von Lerneinheiten sowie das tägliche Arbeitspensum.
 * Gemäß dem Rich Domain Model enthält diese Klasse auch Validierungslogik.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "learning_preferences")
public class LearningPreferences {

    /** Eindeutiger Identifikator der Präferenzen (readOnly). */
    @Id
    @GeneratedValue
    @Column(name = "preference_id", updatable = false)
    private UUID preferenceId;

    /** Die minimale Zeitdauer einer einzelnen Lerneinheit in Minuten. */
    @Column(name = "min_unit_duration_minutes", nullable = false)
    private int minUnitDurationMinutes;

    /** Maximale Dauer einer einzelnen Lerneinheit in Minuten. */
    @Column(name = "max_unit_duration_minutes", nullable = false)
    private int maxUnitDurationMinutes;

    /** Maximale tägliche Arbeitslast in Stunden. */
    @Column(name = "max_daily_workload_hours", nullable = false)
    private int maxDailyWorkloadHours;

    /** Dauer der Pausen zwischen Lerneinheiten in Minuten. */
    @Column(name = "break_duration_minutes", nullable = false)
    private int breakDurationMinutes;

    /** Pufferzeit vor Deadlines in Tagen. */
    @Column(name = "deadline_buffer_days", nullable = false)
    private int deadlineBufferDays;

    /** Liste der bevorzugten Zeitfenster für das Lernen. */
    @ElementCollection(targetClass = TimeSlot.class)
    @CollectionTable(name = "preferred_time_slots", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot")
    private Set<TimeSlot> preferredTimeSlots;

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "preferred_week_days", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> preferredDays;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     * Ermöglicht die Instanziierung durch das Framework.
     */
    protected LearningPreferences() {
    }

    /**
     * Erzeugt eine neue Instanz der Lernpräferenzen mit den angegebenen Parametern.
     * Die UUID wird durch ORM automatisch generiert.
     *
     * @param minUnitDurationMinutes Die Untergrenze für die Dauer einer Lerneinheit (in Minuten).
     * @param maxUnitDurationMinutes Die Obergrenze für die Dauer einer Lerneinheit (in Minuten).
     * @param maxDailyWorkloadHours  Das angestrebte maximale Tagespensum an Lernzeit (in Stunden).
     * @param breakDurationMinutes   Die Standarddauer für Pausen zwischen Lerneinheiten (in Minuten).
     * @param deadlineBufferDays     Der Pufferzeitraum, der vor Abgabefristen eingeplant werden soll (in Tagen).
     * @param preferredTimeSlots     Die Menge der bevorzugten Zeitfenster für Lerneinheiten.
     * @param preferredDays          Die Menge der bevorzugten Wochentage für Lerneinheiten.
     */
    public LearningPreferences(int minUnitDurationMinutes,
                               int maxUnitDurationMinutes,
                               int maxDailyWorkloadHours,
                               int breakDurationMinutes,
                               int deadlineBufferDays,
                               Set<TimeSlot> preferredTimeSlots,
                               Set<DayOfWeek> preferredDays

    ) {
        this.minUnitDurationMinutes = minUnitDurationMinutes;
        this.maxUnitDurationMinutes = maxUnitDurationMinutes;
        this.maxDailyWorkloadHours = maxDailyWorkloadHours;
        this.breakDurationMinutes = breakDurationMinutes;
        this.deadlineBufferDays = deadlineBufferDays;
        this.preferredTimeSlots = preferredTimeSlots != null ? preferredTimeSlots : new HashSet<>();
        this.preferredDays = preferredDays != null ? preferredDays : new HashSet<>();
    }
    // --- Getter, Setter & Helper ---

    /** @return Die Menge der bevorzugten Wochentage für Lerneinheiten. */
    public Set<DayOfWeek> getPreferredDays() {
        return preferredDays;
    }

    /**
     * Gibt die Menge der bevorzugten Zeitslots zurück.
     * @return Die Menge der bevorzugten Zeitslots oder null, wenn keine definiert sind.
     */
    public Set<TimeSlot> getPreferredTimeSlots() {
        return preferredTimeSlots;
    }

    /** @param preferredDays Die neu zu setzende Menge an bevorzugten Wochentagen. */
    public void setPreferredDays(Set<DayOfWeek> preferredDays) {
        this.preferredDays = preferredDays;
    }

    /** @return Die eindeutige Kennung dieser Präferenz-Konfiguration. */
    public UUID getPreferenceId() {
        return preferenceId;
    }

    /**
     * Setzt die Pausendauer zwischen Lerneinheiten.
     * @param breakDurationMinutes Pausendauer in Minuten
     */
    public void setBreakDurationMinutes(int breakDurationMinutes) {
        this.breakDurationMinutes = breakDurationMinutes;
    }

    /**
     * Setzt den Puffer vor Deadlines.
     * @param deadlineBufferDays Puffer in Tagen
     */
    public void setDeadlineBufferDays(int deadlineBufferDays) {
        this.deadlineBufferDays = deadlineBufferDays;
    }

    /**
     * Setzt die maximale tägliche Arbeitslast.
     * @param maxDailyWorkloadHours Arbeitslast in Stunden
     */
    public void setMaxDailyWorkloadHours(int maxDailyWorkloadHours) {
        this.maxDailyWorkloadHours = maxDailyWorkloadHours;
    }

    /**
     * Setzt die maximale Dauer einer Lerneinheit.
     * @param maxUnitDurationMinutes Dauer in Minuten
     */
    public void setMaxUnitDurationMinutes(int maxUnitDurationMinutes) {
        this.maxUnitDurationMinutes = maxUnitDurationMinutes;
    }

    /**
     * Setzt die minimale Dauer einer Lerneinheit.
     * @param minUnitDurationMinutes Dauer in Minuten
     */
    public void setMinUnitDurationMinutes(int minUnitDurationMinutes) {
        this.minUnitDurationMinutes = minUnitDurationMinutes;
    }

    /**
     * Setzt die bevorzugten Zeitslots.
     * @param preferredTimeSlots Menge der Zeitslots
     */
    public void setPreferredTimeSlots(Set<TimeSlot> preferredTimeSlots) {
        this.preferredTimeSlots = preferredTimeSlots;
    }

    /**
     * Gibt die minimale Dauer einer Lerneinheit in Minuten zurück.
     * @return Minimale Dauer in Minuten
     */
    public int getMinUnitDurationMinutes() {
        return minUnitDurationMinutes;
    }
    /**
     * Gibt die maximale Dauer einer Lerneinheit in Minuten zurück.
     * @return Maximale Dauer in Minuten
     */
    public int getMaxUnitDurationMinutes() {
        return maxUnitDurationMinutes;
    }
    /**
     * Gibt die maximale tägliche Arbeitslast in Stunden zurück.
     * @return Maximale Arbeitslast in Stunden
     */
    public int getMaxDailyWorkloadHours() {
        return maxDailyWorkloadHours;
    }

    /**
     * Gibt die Pausendauer zwischen Lerneinheiten in Minuten zurück.
     * @return Pausendauer in Minuten
     */
    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    /**
     * Gibt die Pufferzeit vor Deadlines in Tagen zurück.
     * @return Pufferzeit in Tagen
     */
    public int getDeadlineBufferDays() {
        return deadlineBufferDays;
    }
}
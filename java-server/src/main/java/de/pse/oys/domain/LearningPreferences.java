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
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "learning_preferences")
public class LearningPreferences {

    /** Eindeutiger Identifikator der Präferenzen (readOnly). */
    @Id
    @Column(name = "preference_id", updatable = false)
    private UUID preferenceId;

    /** Die minimale Zeitdauer einer einzelnen Lerneinheit in Minuten. */
    @Column(name = "min_unit_duration_minutes", nullable = false)
    private int minUnitDurationMinutes;

    /** Maximale Dauer einer einzelnen Lerneinheit in Minuten. */
    @Column(name = "max_unit_duration_minutes", nullable = false) //bin mir bei nullable nicht ganz sicher
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
    private List<TimeSlot> preferredTimeSlots = new ArrayList<>();

    // TODO: @Marcel kannst du mal drüber schauen ob das so passt mit den preferredDays?
    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "preferred_week_days", joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> preferredDays = new HashSet<>();

    /**
     * Standardkonstruktor für JPA/Hibernate.
     * Ermöglicht die Instanziierung durch das Framework.
     */
    protected LearningPreferences() {
    }

    /**
     * Erzeugt eine neue Instanz der Lernpräferenzen mit den angegebenen Parametern.
     *
     * @param preferenceId           Die eindeutige UUID zur Identifizierung dieses Präferenzsatzes.
     * @param minUnitDurationMinutes Die Untergrenze für die Dauer einer Lerneinheit (in Minuten).
     * @param maxUnitDurationMinutes Die Obergrenze für die Dauer einer Lerneinheit (in Minuten).
     * @param maxDailyWorkloadHours  Das angestrebte maximale Tagespensum an Lernzeit (in Stunden).
     * @param breakDurationMinutes   Die Standarddauer für Pausen zwischen Lerneinheiten (in Minuten).
     * @param deadlineBufferDays     Der Pufferzeitraum, der vor Abgabefristen eingeplant werden soll (in Tagen).
     */
    public LearningPreferences(UUID preferenceId, int minUnitDurationMinutes, int maxUnitDurationMinutes, int maxDailyWorkloadHours, int breakDurationMinutes, int deadlineBufferDays) {
        this.preferenceId = preferenceId;
        this.minUnitDurationMinutes = minUnitDurationMinutes;
        this.maxUnitDurationMinutes = maxUnitDurationMinutes;
        this.maxDailyWorkloadHours = maxDailyWorkloadHours;
        this.breakDurationMinutes = breakDurationMinutes;
        this.deadlineBufferDays = deadlineBufferDays;
    }

    /**
     * Überprüft die logische Konsistenz bei der Änderung der maximalen Lerndauer pro Einheit.
     * Stellt sicher, dass der neue Wert nicht unter der aktuell definierten minimalen Dauer liegt.
     *
     * @param minutes Die geplante maximale Dauer in Minuten.
     * @return true, wenn die neue maximale Dauer gültig ist.
     */
    public boolean isValidNewMaxDuration(int minutes) {
        return minutes >= this.minUnitDurationMinutes;
    }

    /**
     * Validiert die minimale Lerndauer.
     * Garantiert, dass die Untergrenze für eine Lerneinheit die definierte Obergrenze nicht überschreitet.
     *
     * @param minutes Die geplante minimale Dauer in Minuten.
     * @return true, wenn die neue minimale Dauer gültig ist.
     */
    public boolean isValidNewMinDuration(int minutes) {
        return minutes <= this.maxUnitDurationMinutes;
    }

    /**
     * Fügt einen bevorzugten Zeitslot zur Liste hinzu.
     * @param slot Der hinzuzufügende {@link TimeSlot}.
     */
    public void addSlot(TimeSlot slot) {
        if (!this.preferredTimeSlots.contains(slot)) {
            this.preferredTimeSlots.add(slot);
        }
    }

    /**
     * Entfernt einen bevorzugten Zeitslot aus der Liste.
     * @param slot Der zu entfernende {@link TimeSlot}.
     */
    public void removeSlot(TimeSlot slot) {
        this.preferredTimeSlots.remove(slot);
    }



    // --- Getter, Setter & Helper ---

    /** @return Die Menge der bevorzugten Wochentage für Lerneinheiten. */
    public Set<DayOfWeek> getPreferredDays() {
        return preferredDays;
    }

    /** @param preferredDays Die neu zu setzende Menge an bevorzugten Wochentagen. */
    public void setPreferredDays(Set<DayOfWeek> preferredDays) {
        this.preferredDays = preferredDays;
    }

    /** @param day Ein Wochentag, der zu den bevorzugten Tagen hinzugefügt werden soll. */
    public void addPreferredDay(DayOfWeek day) {
        this.preferredDays.add(day);
    }

    /** @param day Der Wochentag, der aus den bevorzugten Tagen entfernt werden soll. */
    public void removePreferredDay(DayOfWeek day) {
        this.preferredDays.remove(day);
    }

    /** @return Die eindeutige Kennung dieser Präferenz-Konfiguration. */
    public UUID getPreferenceId() {
        return preferenceId;
    }


}
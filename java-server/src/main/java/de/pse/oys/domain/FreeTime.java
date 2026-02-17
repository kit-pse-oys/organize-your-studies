package de.pse.oys.domain;

import de.pse.oys.domain.enums.RecurrenceType;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Repräsentiert einen Zeitraum, in dem der Nutzer keine Lerneinheiten einplanen möchte.
 * Diese Freizeitblöcke werden vom Planungsalgorithmus als harte Restriktionen behandelt.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "free_times")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "recurrence_type_discriminator", discriminatorType = DiscriminatorType.STRING)
public abstract class FreeTime {

    /** Eindeutige Kennung des Freizeitblocks (readOnly). */
    @Id
    @GeneratedValue
    @Column(name = "slotid", updatable = false)
    private UUID freeTimeId;

    /**
     * Zugehöriger Nutzer dieses Freizeitblocks.
     * Wird über die Fremdschlüsselspalte {@code user_id} persistiert.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    /** Kurze Beschreibung oder Name der Freizeitaktivität. */
    @Column(name = "title", nullable = false)
    private String title;

    /** Der Startzeitpunkt des Freizeitblocks. */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Der Endzeitpunkt des Freizeitblocks. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected FreeTime() {
    }

    /**
     * Erzeugt einen neuen Freizeitblock.
     *
     * @param user      Nutzer, dem der Freizeitblock zugeordnet ist.
     * @param title     Name/Beschreibung (z. B. "Fußballtraining").
     * @param startTime Beginn der Freizeit.
     * @param endTime   Ende der Freizeit.
     */
    protected FreeTime(User user, String title, LocalTime startTime, LocalTime endTime) {
        this.user = user;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Gibt den Typ der Wiederholung zurück.
     * Das Feld wird über die Unterklassen gesteuert.
     *
     * @return Der spezifische Wiederholungstyp dieser Freizeitinstanz.
     */
    @Transient
    public abstract RecurrenceType getRecurrenceType();

    /**
     * Prüft, ob diese Freizeit an einem bestimmten Datum "gilt".
     * SingleFreeTime: Datum muss exakt übereinstimmen.
     * RecurringFreeTime: Wochentag muss übereinstimmen.
     *
     * @param date Das zu prüfende Datum.
     * @return true, wenn die Freizeit an diesem Datum gilt.
     */
    public abstract boolean occursOn(LocalDate date);

    /**
     * Liefert das Datum, das im DTO-Feld "date" zurückgegeben werden soll.
     * - SingleFreeTime: echtes Datum
     * - RecurringFreeTime: repräsentatives Datum, das den Wochentag kodiert
     *
     * @return DTO-kompatibles Datum.
     */
    public abstract LocalDate getRepresentativeDate();

    /**
     * Aktualisiert subtype-spezifische Felder anhand des DTO-Datums.
     * - SingleFreeTime: setzt das konkrete Datum
     * - RecurringFreeTime: setzt den Wochentag (aus date.getDayOfWeek())
     *
     * Hinweis: Diese Methode führt KEINEN Typwechsel durch.
     *
     * @param date Das neue Datum aus dem DTO.
     */
    public abstract void applyDtoDate(LocalDate date);

    /**
     * Berechnet die Dauer des Freizeitblocks in Minuten.
     * @return Dauer in Minuten.
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Prüft, ob der Zeitraum logisch konsistent ist (Start vor Ende).
     * @return true, wenn Startzeit chronologisch vor Endzeit liegt.
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    /**
     * Prüft, ob dieser Freizeitblock wöchentlich wiederkehrend ist.
     *
     * @return {@code true}, wenn {@link #getRecurrenceType()} {@link RecurrenceType#WEEKLY} liefert,
     *         sonst {@code false}
     */
    @Transient
    public boolean isWeekly() {
        return getRecurrenceType() == RecurrenceType.WEEKLY;
    }

    // Getter

    /** @return Die ID des Freizeitblocks. */
    public UUID getFreeTimeId() {
        return freeTimeId;
    }

    /**
     * @return Die User-ID dieses Freizeitblocks.
     */
    public UUID getUserId() {
        return (user != null) ? user.getId() : null;
    }

    /**
     * @return Den zugehörigen Nutzer (lazy geladen).
     */
    public User getUser() {
        return user;
    }

    /** @return Die Beschreibung der Freizeit. */
    public String getTitle() {
        return title;
    }

    /** @return Den Startzeitpunkt. */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** @return Den Endzeitpunkt. */
    public LocalTime getEndTime() {
        return endTime;
    }

    // Setter

    /**
     * Setzt den Nutzer dieses Freizeitblocks.
     *
     * @param user der zu setzende Nutzer
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @param title Die neue Beschreibung.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param startTime Der neue Startzeitpunkt.
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * @param endTime Der neue Endzeitpunkt.
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}

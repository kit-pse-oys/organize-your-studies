package de.pse.oys.domain;

import de.pse.oys.domain.enums.RecurrenceType;
import jakarta.persistence.*;
import de.pse.oys.persistence.DayOfWeekStringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Repräsentiert eine regelmäßig wiederkehrende Freizeitbeschränkung.
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("WEEKLY")
public class RecurringFreeTime extends FreeTime {

    /** Referenzdatum zur deterministischen Kodierung des Wochentags im DTO. */
    private static final LocalDate WEEKDAY_BASE_MONDAY = LocalDate.of(1970, 1, 5); // Monday

    /** Der Wochentag, an dem die Freizeit stattfindet. */
    @Convert(converter = DayOfWeekStringConverter.class)
    @Column(name = "weekday")
    private DayOfWeek dayOfWeek;

    protected RecurringFreeTime() {
        super();
    }

    /**
     * Erzeugt eine Instanz für eine wiederkehrende Freizeitbeschränkung.
     * Der Typ wird dabei automatisch auf WEEKLY gesetzt.
     *
     * @param userId ID des Nutzers.
     * @param title Bezeichnung der Freizeit (z. B. "Wöchentliches Training").
     * @param start Beginn der Freizeit als Uhrzeit.
     * @param end   Ende der Freizeit (Uhrzeit).
     * @param day   Der Wochentag, an dem die Wiederholung stattfindet.
     */
    public RecurringFreeTime(UUID userId, String title, LocalTime start, LocalTime end, DayOfWeek day) {
        super(userId, title, start, end);
        this.dayOfWeek = day;
    }

    /**
     * Gibt den Wiederholungstyp dieses Freizeitblocks zurück.
     * Für {@link RecurringFreeTime} ist der Typ immer {@link RecurrenceType#WEEKLY}.
     *
     * @return {@link RecurrenceType#WEEKLY}
     */
    @Override
    public RecurrenceType getRecurrenceType() {
        return RecurrenceType.WEEKLY;
    }

    /**
     * Prüft, ob diese Freizeit an einem bestimmten Datum "gilt".
     * Für {@link RecurringFreeTime} muss der Wochentag übereinstimmen.
     *
     * @param date Das zu prüfende Datum.
     * @return true, wenn der Wochentag übereinstimmt.
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return date != null && date.getDayOfWeek() == this.dayOfWeek;
    }

    /**
     * Liefert das Datum, das im DTO-Feld "date" zurückgegeben werden soll.
     * Bei wöchentlichen FreeTimes wird der Wochentag deterministisch als Datum kodiert:
     * Monday = 1970-01-05, Tuesday = 1970-01-06, ...
     *
     * @return Repräsentatives Datum für den Wochentag oder null, wenn dayOfWeek nicht gesetzt ist.
     */
    @Override
    public LocalDate getRepresentativeDate() {
        if (dayOfWeek == null) {
            return null;
        }
        return WEEKDAY_BASE_MONDAY.plusDays(dayOfWeek.getValue() - 1L);
    }

    /**
     * Aktualisiert subtype-spezifische Felder anhand des DTO-Datums.
     * Bei {@link RecurringFreeTime} wird der Wochentag aus date.getDayOfWeek() abgeleitet.
     *
     * @param date Das neue Datum aus dem DTO (repräsentiert den Wochentag).
     */
    @Override
    public void applyDtoDate(LocalDate date) {
        this.dayOfWeek = (date != null) ? date.getDayOfWeek() : null;
    }

    //GETTER UND SETTER

    /**
     * @return Den Wochentag, an dem diese Freizeitbeschränkung eintritt.
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * @param dayOfWeek Der neue Wochentag für die regelmäßige Wiederholung.
     */
    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}

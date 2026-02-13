package de.pse.oys.domain;

import de.pse.oys.domain.enums.RecurrenceType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Repräsentiert eine einmalige Freizeitbeschränkung an einem spezifischen Datum.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("ONCE")
public class SingleFreeTime extends FreeTime {

    /** Das spezifische Datum dieses Termins. */
    @Column(name = "specific_date")
    private LocalDate date;

    protected SingleFreeTime() {
        super();
    }

    /**
     * Erzeugt eine Instanz für ein einmaliges Freizeitereignis.
     *
     * @param userId ID des Nutzers.
     * @param title Bezeichnung des Ereignisses (z. B. "Arzttermin").
     * @param start Beginn der Freizeit (Uhrzeit).
     * @param end   Ende der Freizeit (Uhrzeit).
     * @param date  Das konkrete Datum des Ereignisses.
     */
    public SingleFreeTime(UUID userId, String title, LocalTime start, LocalTime end, LocalDate date) {
        super(userId, title, start, end);
        this.date = date;
    }

    /**
     * Gibt den Wiederholungstyp dieses Freizeitblocks zurück.
     * Für {@link SingleFreeTime} ist der Typ immer {@link RecurrenceType#ONCE}.
     *
     * @return {@link RecurrenceType#ONCE}
     */
    @Override
    public RecurrenceType getRecurrenceType() {
        return RecurrenceType.ONCE;
    }

    /**
     * Prüft, ob diese Freizeit an einem bestimmten Datum "gilt".
     * Für {@link SingleFreeTime} muss das Datum exakt übereinstimmen.
     *
     * @param date Das zu prüfende Datum.
     * @return true, wenn das Datum exakt übereinstimmt.
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return Objects.equals(this.date, date);
    }

    /**
     * Liefert das Datum, das im DTO-Feld "date" zurückgegeben werden soll.
     * Für {@link SingleFreeTime} ist das das echte Datum dieses Ereignisses.
     *
     * @return Das konkrete Datum dieses Ereignisses.
     */
    @Override
    public LocalDate getRepresentativeDate() {
        return this.date;
    }

    /**
     * Aktualisiert subtype-spezifische Felder anhand des DTO-Datums.
     * Für {@link SingleFreeTime} ist das das konkrete Datum.
     *
     * @param date Das neue konkrete Datum.
     */
    @Override
    public void applyDtoDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Überprüft, ob der Termin der einmaligen Freizeit bereits in der Vergangenheit liegt.
     * @return true, wenn das Datum vor dem heutigen Systemdatum liegt.
     */
    public boolean isPast() {
        return date != null && date.isBefore(LocalDate.now());
    }

    //GETTER UND SETTER

    /**
     * @return Das spezifische Datum dieses Freizeitblocks.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @param date Das neue Datum für diesen Freizeitblock.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }
}
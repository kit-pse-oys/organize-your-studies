package de.pse.oys.domain;

import de.pse.oys.domain.enums.RecurrenceType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Repräsentiert eine einmalige Freizeitbeschränkung an einem spezifischen Datum.
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
     * @param id    Eindeutige ID des Freizeitblocks.
     * @param title Bezeichnung des Ereignisses (z. B. "Arzttermin").
     * @param start Beginn der Freizeit (Uhrzeit).
     * @param end   Ende der Freizeit (Uhrzeit).
     * @param date  Das konkrete Datum des Ereignisses.
     */
    public SingleFreeTime(UUID id, String title, LocalTime start, LocalTime end, LocalDate date) {
        super(id, title, start, end, RecurrenceType.ONCE);
        this.date = date;
    }

    /**
     * Überprüft, ob der Termin der einmaligen Freizeit bereits in der Vergangenheit liegt.
     * * @return true, wenn das Datum vor dem heutigen Systemdatum liegt.
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
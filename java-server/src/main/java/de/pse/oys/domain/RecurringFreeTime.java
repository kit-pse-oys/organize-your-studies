package de.pse.oys.domain;

import de.pse.oys.domain.enums.RecurrenceType;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Repräsentiert eine regelmäßig wiederkehrende Freizeitbeschränkung.
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("WEEKLY")
public class RecurringFreeTime extends FreeTime {

    /** Der Wochentag, an dem die Freizeit stattfindet. */
    @Enumerated(EnumType.STRING)
    @Column(name = "weekday")
    private DayOfWeek dayOfWeek;

    protected RecurringFreeTime() {
        super();
    }

    /**
     * Erzeugt eine Instanz für eine wiederkehrende Freizeitbeschränkung.
     * Der Typ wird dabei automatisch auf WEEKLY gesetzt.
     *
     * @param title Bezeichnung der Freizeit (z. B. "Wöchentliches Training").
     * @param start Beginn der Freizeit als Uhrzeit.
     * @param end   Ende der Freizeit als Uhrzeit.
     * @param day   Der Wochentag, an dem die Wiederholung stattfindet.
     */
    public RecurringFreeTime(String title, LocalTime start, LocalTime end, DayOfWeek day) {
        super(title, start, end, RecurrenceType.WEEKLY);
        this.dayOfWeek = day;
    }

    /**
     * Prüft, ob die Freizeit an einem bestimmten Datum stattfindet.
     * @param date Das zu prüfende Datum.
     * @return true, wenn der Wochentag übereinstimmt.
     */
    public boolean occursOn(LocalDate date) {
        return date != null && date.getDayOfWeek() == this.dayOfWeek;
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
package de.pse.oys.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO für FreeTime in API-Responses.
 *
 * Hinweis zur Bedeutung von "date":
 * - weekly=false: echtes Datum der einmaligen Freizeit
 * - weekly=true : Datum dient nur dazu, den Wochentag zu transportieren (getDayOfWeek()).
 */
public class FreeTimeDTO {

    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean weekly;

    /** No-Args-Konstruktor für Jackson. */
    public FreeTimeDTO() {
        // empty
    }

    /**
     * Erstellt ein FreeTimeDTO.
     *
     * @param title     Titel/Bezeichnung
     * @param date      Datum (bei weekly repräsentiert es den Wochentag)
     * @param startTime Startzeit
     * @param endTime   Endzeit
     * @param weekly    {@code true} wenn wöchentlich, sonst {@code false}
     */
    public FreeTimeDTO(String title, LocalDate date, LocalTime startTime, LocalTime endTime, boolean weekly) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekly = weekly;
    }

    /** @return der Titel der Freizeit */
    public String getTitle() {
        return title;
    }

    /** @param title der neue Titel der Freizeit */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return das Datum der Freizeit */
    public LocalDate getDate() {
        return date;
    }

    /** @param date das neue Datum der Freizeit */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** @return die Startzeit */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** @param startTime die neue Startzeit */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /** @return die Endzeit */
    public LocalTime getEndTime() {
        return endTime;
    }

    /** @param endTime die neue Endzeit */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /** @return {@code true} wenn wöchentlich, sonst {@code false} */
    public boolean isWeekly() {
        return weekly;
    }

    /** @param weekly {@code true} für wöchentlich, {@code false} für einmalig */
    public void setWeekly(boolean weekly) {
        this.weekly = weekly;
    }
}
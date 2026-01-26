package de.pse.oys.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Data Transfer Object für Freizeiträume.
 * Repräsentiert ein Zeitfenster, in dem der Nutzer Zeit zum Lernen hat.
 * @author utgid
 * @version 1.0
 */
public class FreeTimeDTO {

    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean weekly;

    /**
     * Standardkonstruktor für die Deserialisierung (z.B. durch Jackson).
     */
    public FreeTimeDTO() {}

    // Getter und Setter

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isWeekly() { return weekly; }
    public void setWeekly(boolean weekly) { this.weekly = weekly; }
}
package de.pse.oys.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datentransferobjekt für eine einzelne Lerneinheit.
 * Die Struktur folgt strikt den Vorgaben für die Kalenderanzeige im Frontend.
 */
public class UnitDTO {

    /** Der Titel der Lerneinheit. */
    private String title;

    /** Eine detaillierte Beschreibung der Einheit. */
    private String description;

    /** Der hexadezimale Farbcode zur Darstellung im UI. */
    private String color;

    /** Das Datum der Einheit im Format YYYY-MM-DD. */
    private LocalDate date;

    /** Die Startzeit der Einheit im Format HH:mm. */
    private LocalTime start;

    /** Die Endzeit der Einheit im Format HH:mm. */
    private LocalTime end;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public UnitDTO() {
    }

    // Getter

    /** @return Der Titel der Einheit. */
    public String getTitle() { return title; }

    /** @return Die Beschreibung der Einheit. */
    public String getDescription() { return description; }

    /** @return Der Farbcode (HEX). */
    public String getColor() { return color; }

    /** @return Das Datum der Durchführung. */
    public LocalDate getDate() { return date; }

    /** @return Die Startuhrzeit. */
    public LocalTime getStart() { return start; }

    /** @return Die Enduhrzeit. */
    public LocalTime getEnd() { return end; }

    // Setter

    /** @param title Der Titel der Einheit. */
    public void setTitle(String title) { this.title = title; }

    /** @param description Die Beschreibung der Einheit. */
    public void setDescription(String description) { this.description = description; }

    /** @param color Der Farbcode als String. */
    public void setColor(String color) { this.color = color; }

    /** @param date Das Datum (LocalDate). */
    public void setDate(LocalDate date) { this.date = date; }

    /** @param start Die Startzeit (LocalTime). */
    public void setStart(LocalTime start) { this.start = start; }

    /** @param end Die Endzeit (LocalTime). */
    public void setEnd(LocalTime end) { this.end = end; }
}
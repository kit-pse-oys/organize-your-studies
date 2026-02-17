package de.pse.oys.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Datentransferobjekt für eine einzelne Lerneinheit.
 * Die Struktur folgt strikt den Vorgaben für die Kalenderanzeige im Frontend.
 */
public class UnitDTO {

    /** Das Datum der Einheit im Format YYYY-MM-DD. */
    private LocalDate date;

    /** Die Startzeit der Einheit im Format HH:mm. */
    private LocalTime start;

    /** Die Endzeit der Einheit im Format HH:mm. */
    private LocalTime end;

    /** Die zugehörige Aufgabe der Lerneinheit. */
    private UUID taskId;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public UnitDTO() {
        // Standardkonstruktor
    }

    // Getter

    /** @return Das Datum der Durchführung. */
    public LocalDate getDate() { return date; }

    /** @return Die Startuhrzeit. */
    public LocalTime getStart() { return start; }

    /** @return Die Enduhrzeit. */
    public LocalTime getEnd() { return end; }

    /** Gibt den zugehörigen Task zurück.
     * @return Die UUID der zugehörigen Aufgabe.
     */
    public UUID getTaskId() { return taskId; }


    // Setter

    /** @param date Das Datum (LocalDate). */
    public void setDate(LocalDate date) { this.date = date; }

    /** @param start Die Startzeit (LocalTime). */
    public void setStart(LocalTime start) { this.start = start; }

    /** @param end Die Endzeit (LocalTime). */
    public void setEnd(LocalTime end) { this.end = end; }

    /** Setzt den zugehörigen Task. */
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
}
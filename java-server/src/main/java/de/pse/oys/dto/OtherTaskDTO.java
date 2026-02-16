package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO für "OTHER"-Tasks mit Start- und Endzeitpunkt (LocalDateTime).
 *
 * @author uqvfm
 */
public class OtherTaskDTO extends TaskDTO {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * Default-Konstruktor (z.B. für JSON).
     */
    public OtherTaskDTO() {
        super();
    }

    /**
     * Erzeugt ein OtherTaskDTO.
     *
     * @param title          Titel der Aufgabe
     * @param moduleId       Titel des Moduls
     * @param weeklyTimeLoad Wöchentlicher Aufwand (Minuten)
     * @param startTime      Startzeitpunkt
     * @param endTime        Endzeitpunkt
     */
    public OtherTaskDTO(String title,
                        UUID moduleId,
                        Integer weeklyTimeLoad,
                        LocalDateTime startTime,
                        LocalDateTime endTime) {
        super(title, moduleId, TaskCategory.OTHER, weeklyTimeLoad);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Gibt den Startzeitpunkt zurück.
     *
     * @return Startzeitpunkt
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Setzt den Startzeitpunkt.
     *
     * @param startTime Startzeitpunkt
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gibt den Endzeitpunkt zurück.
     *
     * @return Endzeitpunkt
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Setzt den Endzeitpunkt.
     *
     * @param endTime Endzeitpunkt
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}

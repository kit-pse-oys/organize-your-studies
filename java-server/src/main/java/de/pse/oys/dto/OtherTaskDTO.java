package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;

import java.time.LocalDateTime;

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
     * @param title            Titel der Aufgabe
     * @param moduleTitle      Titel des Moduls
     * @param weeklyTimeLoad   Wöchentlicher Aufwand (Minuten)
     * @param sendNotification Ob Benachrichtigungen aktiv sind
     * @param startTime        Startzeitpunkt
     * @param endTime          Endzeitpunkt
     */
    public OtherTaskDTO(String title,
                        String moduleTitle,
                        Integer weeklyTimeLoad,
                        boolean sendNotification,
                        LocalDateTime startTime,
                        LocalDateTime endTime) {
        super(title, moduleTitle, TaskCategory.OTHER, weeklyTimeLoad, sendNotification);
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

package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;

import java.time.LocalDate;

/**
 * DTO für "OTHER"-Tasks mit Start- und Enddatum.
 */
public class OtherTaskDTO extends TaskDTO {

    private LocalDate startDate;
    private LocalDate endDate;

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
     * @param startDate        Startdatum
     * @param endDate          Enddatum
     */
    public OtherTaskDTO(String title,
                        String moduleTitle,
                        Integer weeklyTimeLoad,
                        boolean sendNotification,
                        LocalDate startDate,
                        LocalDate endDate) {
        super(title, moduleTitle, TaskCategory.OTHER, weeklyTimeLoad, sendNotification);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Gibt das Startdatum zurück.
     *
     * @return Startdatum
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Setzt das Startdatum.
     *
     * @param startDate Startdatum
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gibt das Enddatum zurück.
     *
     * @return Enddatum
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Setzt das Enddatum.
     *
     * @param endDate Enddatum
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

package de.pse.oys.dto;

import java.time.LocalTime;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.Weekday;

/**
 * DTO für Aufgaben mit wiederkehrender Abgabe.
 */
public class SubmissionTaskDTO extends TaskDTO {

    private Weekday submissionDay;
    private LocalTime submissionTime;
    private Integer submissionCycle;

    /**
     * Default-Konstruktor (z.B. für Deserialisierung).
     */
    public SubmissionTaskDTO() {
        super();
    }

    /**
     * Erstellt ein SubmissionTaskDTO.
     *
     * @param title            Titel der Aufgabe
     * @param moduleTitle      Titel des zugehörigen Moduls
     * @param weeklyTimeLoad   wöchentlicher Zeitaufwand (z.B. in Minuten)
     * @param sendNotification ob Benachrichtigungen gesendet werden sollen
     * @param submissionDay    Wochentag der Abgabe
     * @param submissionTime   Uhrzeit der Abgabe
     * @param submissionCycle  Zyklus/Intervall (z.B. 1 = wöchentlich)
     */
    public SubmissionTaskDTO(String title,
                             String moduleTitle,
                             Integer weeklyTimeLoad,
                             boolean sendNotification,
                             Weekday submissionDay,
                             LocalTime submissionTime,
                             Integer submissionCycle) {
        super(title, moduleTitle, TaskCategory.SUBMISSION, weeklyTimeLoad, sendNotification);
        this.submissionDay = submissionDay;
        this.submissionTime = submissionTime;
        this.submissionCycle = submissionCycle;
    }

    /**
     * Liefert den Wochentag der Abgabe.
     *
     * @return Wochentag der Abgabe
     */
    public Weekday getSubmissionDay() {
        return submissionDay;
    }

    /**
     * Setzt den Wochentag der Abgabe.
     *
     * @param submissionDay Wochentag der Abgabe
     */
    public void setSubmissionDay(Weekday submissionDay) {
        this.submissionDay = submissionDay;
    }

    /**
     * Liefert die Uhrzeit der Abgabe.
     *
     * @return Uhrzeit der Abgabe
     */
    public LocalTime getSubmissionTime() {
        return submissionTime;
    }

    /**
     * Setzt die Uhrzeit der Abgabe.
     *
     * @param submissionTime Uhrzeit der Abgabe
     */
    public void setSubmissionTime(LocalTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    /**
     * Liefert den Abgabe-Zyklus.
     *
     * @return Zyklus/Intervall (z.B. 1 = wöchentlich)
     */
    public Integer getSubmissionCycle() {
        return submissionCycle;
    }

    /**
     * Setzt den Abgabe-Zyklus.
     *
     * @param submissionCycle Zyklus/Intervall (z.B. 1 = wöchentlich)
     */
    public void setSubmissionCycle(Integer submissionCycle) {
        this.submissionCycle = submissionCycle;
    }
}

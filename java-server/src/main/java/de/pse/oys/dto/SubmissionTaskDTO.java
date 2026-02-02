package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;

import java.time.LocalDateTime;

/**
 * DTO für Aufgaben mit wiederkehrenden Abgaben (SubmissionTask).
 */
public class SubmissionTaskDTO extends TaskDTO {

    /** Erste Deadline (Anchor), an der sich die Wiederholung ausrichtet. */
    private LocalDateTime firstDeadline;

    /** Rhythmus in Wochen (1 = wöchentlich, 2 = alle 2 Wochen, ...). */
    private Integer submissionCycle;

    /** Zeitpunkt, ab dem die Aufgabe komplett vorbei ist (keine Deadlines mehr). */
    private LocalDateTime endTime;

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
     * @param firstDeadline    erste Deadline (Anchor)
     * @param submissionCycle  Zyklus/Intervall in Wochen (1 = wöchentlich)
     * @param endTime          Ende der wiederkehrenden Abgaben
     */
    public SubmissionTaskDTO(String title,
                             String moduleTitle,
                             Integer weeklyTimeLoad,
                             boolean sendNotification,
                             LocalDateTime firstDeadline,
                             Integer submissionCycle,
                             LocalDateTime endTime) {
        super(title, moduleTitle, TaskCategory.SUBMISSION, weeklyTimeLoad, sendNotification);
        this.firstDeadline = firstDeadline;
        this.submissionCycle = submissionCycle;
        this.endTime = endTime;
    }

    /**
     * Liefert die erste Deadline (Anchor).
     *
     * @return erste Deadline
     */
    public LocalDateTime getFirstDeadline() {
        return firstDeadline;
    }

    /**
     * Setzt die erste Deadline (Anchor).
     *
     * @param firstDeadline erste Deadline
     */
    public void setFirstDeadline(LocalDateTime firstDeadline) {
        this.firstDeadline = firstDeadline;
    }

    /**
     * Liefert den Abgabe-Zyklus in Wochen.
     *
     * @return Zyklus/Intervall in Wochen
     */
    public Integer getSubmissionCycle() {
        return submissionCycle;
    }

    /**
     * Setzt den Abgabe-Zyklus in Wochen.
     *
     * @param submissionCycle Zyklus/Intervall in Wochen (>= 1)
     */
    public void setSubmissionCycle(Integer submissionCycle) {
        this.submissionCycle = submissionCycle;
    }

    /**
     * Liefert das Ende der wiederkehrenden Abgaben.
     *
     * @return Endzeitpunkt
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Setzt das Ende der wiederkehrenden Abgaben.
     *
     * @param endTime Endzeitpunkt
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
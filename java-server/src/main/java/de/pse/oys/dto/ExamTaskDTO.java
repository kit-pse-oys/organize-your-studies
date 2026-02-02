package de.pse.oys.dto;

import java.time.LocalDate;

import de.pse.oys.domain.enums.TaskCategory;

/**
 * DTO für Aufgaben mit einem festen Prüfungstermin.
 */
public class ExamTaskDTO extends TaskDTO {

    private LocalDate examDate;

    /**
     * Default-Konstruktor (z.B. für Deserialisierung).
     */
    public ExamTaskDTO() {
        super();
    }

    /**
     * Erstellt ein ExamTaskDTO.
     *
     * @param title            Titel der Aufgabe
     * @param moduleTitle      Titel des zugehörigen Moduls
     * @param weeklyTimeLoad   wöchentlicher Zeitaufwand (z.B. in Minuten)
     * @param examDate         Datum der Prüfung
     */
    public ExamTaskDTO(String title,
                       String moduleTitle,
                       Integer weeklyTimeLoad,
                       LocalDate examDate) {
        super(title, moduleTitle, TaskCategory.EXAM, weeklyTimeLoad);
        this.examDate = examDate;
    }

    /**
     * Liefert das Prüfungsdatum.
     *
     * @return Prüfungsdatum
     */
    public LocalDate getExamDate() {
        return examDate;
    }

    /**
     * Setzt das Prüfungsdatum.
     *
     * @param examDate Prüfungsdatum
     */
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }
}

package de.pse.oys.dto;

import java.time.LocalDate;
import java.util.UUID;

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
     * @param title          Titel der Aufgabe
     * @param moduleId       Titel des zugehörigen Moduls
     * @param weeklyTimeLoad wöchentlicher Zeitaufwand (z.B. in Minuten)
     * @param examDate       Datum der Prüfung
     */
    public ExamTaskDTO(String title,
                       UUID moduleId,
                       Integer weeklyTimeLoad,
                       LocalDate examDate) {
        super(title, moduleId, TaskCategory.EXAM, weeklyTimeLoad);
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

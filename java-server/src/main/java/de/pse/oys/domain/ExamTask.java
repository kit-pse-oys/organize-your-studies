package de.pse.oys.domain;

import de.pse.oys.domain.enums.TaskCategory;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Repräsentiert die Vorbereitung auf eine Modulprüfung.
 * Die harte Deadline entspricht hierbei dem Zeitpunkt des Prüfungsantritts.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("EXAM")
public class ExamTask extends Task {

    /**
     * Das Datum der Prüfung.
     * Gemappt auf die Spalte 'fixed_deadline' laut ER-Diagramm.
     */
    @Column(name = "fixed_deadline")
    private LocalDate examDate;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected ExamTask() {
        super();
    }

    /**
     * Erzeugt eine neue Prüfungsvorbereitungs-Aufgabe.
     *
     * @param title                 Titel der Aufgabe.
     * @param weeklyDurationMinutes Wöchentlicher Aufwand in Minuten.
     * @param examDate              Zeitpunkt der Prüfung.
     */
    public ExamTask(String title, int weeklyDurationMinutes, LocalDate examDate) {
        super(title, weeklyDurationMinutes, TaskCategory.EXAM);
        this.examDate = examDate;
    }


    /**
     * Gibt das Prüfungsdatum als harten Endpunkt für die Vorbereitungsphase zurück.
     * Da die Vorbereitung vor dem Prüfungstag abgeschlossen sein muss, wird
     * das Datum in einen Zeitstempel zum Beginn des Tages (00:00 Uhr) umgewandelt.
     *
     * @return Das Datum der Prüfung als LocalDateTime zu Beginn des Tages.
     */
    @Override
    public LocalDateTime getHardDeadline() {
        if (examDate == null) {
            return null;
        }
        // Wandelt LocalDate in LocalDateTime um (00:00:00 Uhr am Prüfungstag)
        return examDate.atStartOfDay();
    }

    /**
     * Prüft, ob die Aufgabe aktuell aktiv ist.
     * Eine {@link ExamTask} ist aktiv, solange der aktuelle Zeitpunkt vor der harten Deadline liegt.
     *
     * @return {@code true}, wenn die Prüfung noch nicht erreicht ist, sonst {@code false}.
     */
    @Override
    protected boolean isActive() {
        LocalDateTime examDateTime = getHardDeadline();
        return examDateTime != null && LocalDateTime.now().isBefore(examDateTime);
    }

    // Getter & Setter

    /**
     * @return Das aktuell gesetzte Prüfungsdatum.
     */
    public LocalDate getExamDate() {
        return examDate;
    }

    /**
     * @param examDate Das neue Datum der Prüfung.
     */
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

}
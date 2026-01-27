package de.pse.oys.dto;

import java.time.DayOfWeek;
import java.util.Set;

import de.pse.oys.domain.enums.TimeSlot;


/**
 * DTO zur Beantwortung und Bearbeitung des Fragebogens.
 * Enthält alle vom Nutzer angegebenen Lernpräferenzen.
 * Im Prozess der Verarbeitung werden diese Präferenzen auf das
 * ensprechende Format der {@link de.pse.oys.domain.LearningPreferences} gemappt
 * und persistent gespeichert.
 *
 * @author uhupo
 * @version 1.0
 */
public class QuestionnaireDTO {
    /**
     * Präferierte maximale Dauer einer Lerneinheit in Minuten.
     */
    private Integer maxUnitDuration;

    /**
     * Präferierte minimale Dauer einer Lerneinheit in Minuten.
     */
    private Integer minUnitDuration;

    /**
     * Präferierte Lernzeiten am Tag.
     */
    private Set<TimeSlot> preferredStudyTimes;

    /**
     * Maximales Lernpensum pro Tag in Stunden.
     */
    private Integer maxDayLoad;

    /**
     * Gewünschter zeitlicher Puffer vor Deadlines in Tagen.
     */
    private Integer timeBeforeDeadlines;

    /**
     * Gewünschte Lerntage der Woche.
     */
    private Set<DayOfWeek> preferredStudyDays;

    /**
     * Präferierte Pausendauer zwischen Lerneinheiten in Minuten.
     */
    private Integer preferredPauseDuration;


    /**
     * Parameterloser Konstruktor für QuestionnaireDTO.
     * Notwendig für das Mapping von JSON-Daten.
     */
    public QuestionnaireDTO() {
        // Standardkonstruktor
    }

    // Getter & Setter

    /**
     * Getter für die maximale Lerneinheitsdauer.
     * @return Maximale Lerneinheitsdauer in Minuten.
     */
    public Integer getMaxUnitDuration() {
        return maxUnitDuration;
    }

    /**
     * Setter für die maximale Lerneinheitsdauer.
     * @param maxUnitDuration Maximale Lerneinheitsdauer in Minuten.
     */
    public void setMaxUnitDuration(Integer maxUnitDuration) {
        this.maxUnitDuration = maxUnitDuration;
    }

    /**
     * Getter für die minimale Lerneinheitsdauer.
     * @return Minimale Lerneinheitsdauer in Minuten.
     */
    public Integer getMinUnitDuration() {
        return minUnitDuration;
    }

    /**
     * Setter für die minimale Lerneinheitsdauer.
     * @param minUnitDuration Minimale Lerneinheitsdauer in Minuten.
     */
    public void setMinUnitDuration(Integer minUnitDuration) {
        this.minUnitDuration = minUnitDuration;
    }

    /**
     * Getter für die präferierten Lernzeiten.
     * @return Liste der präferierten Lernzeiten.
     */
    public Set<TimeSlot> getPreferredStudyTimes() {
        return preferredStudyTimes;
    }

    /**
     * Setter für die präferierten Lernzeiten.
     * @param preferredStudyTimes Liste der präferierten Lernzeiten.
     */
    public void setPreferredStudyTimes(Set<TimeSlot> preferredStudyTimes) {
        this.preferredStudyTimes = preferredStudyTimes;
    }

    /**
     * Getter für das maximale Lernpensum pro Tag.
     * @return Maximales Lernpensum pro Tag in Stunden.
     */
    public Integer getMaxDayLoad() {
        return maxDayLoad;
    }

    /**
     * Setter für das maximale Lernpensum pro Tag.
     * @param maxDayLoad Maximales Lernpensum pro Tag in Stunden.
     */
    public void setMaxDayLoad(Integer maxDayLoad) {
        this.maxDayLoad = maxDayLoad;
    }

    /**
     * Getter für den zeitlichen Puffer vor Deadlines.
     * @return Zeitlicher Puffer vor Deadlines in Tagen.
     */
    public Integer getTimeBeforeDeadlines() {
        return timeBeforeDeadlines;
    }

    /**
     * Setter für den zeitlichen Puffer vor Deadlines.
     * @param timeBeforeDeadlines Zeitlicher Puffer vor Deadlines in Tagen.
     */
    public void setTimeBeforeDeadlines(Integer timeBeforeDeadlines) {
        this.timeBeforeDeadlines = timeBeforeDeadlines;
    }

    /**
     * Getter für die präferierten Lerntage der Woche.
     * @return Liste der präferierten Lerntage.
     */
    public Set<DayOfWeek> getPreferredStudyDays() {
        return preferredStudyDays;
    }

    /**
     * Setter für die präferierten Lerntage der Woche.
     * @param preferredStudyDays Liste der präferierten Lerntage.
     */
    public void setPreferredStudyDays(Set<DayOfWeek> preferredStudyDays) {
        this.preferredStudyDays = preferredStudyDays;
    }

    /**
     * Getter für die präferierte Pausendauer.
     * @return Präferierte Pausendauer in Minuten.
     */
    public Integer getPreferredPauseDuration() {
        return preferredPauseDuration;
    }

    /**
     * Setter für die präferierte Pausendauer.
     * @param preferredPauseDuration Präferierte Pausendauer in Minuten.
     */
    public void setPreferredPauseDuration(Integer preferredPauseDuration) {
        this.preferredPauseDuration = preferredPauseDuration;
    }
}
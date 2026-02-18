package de.pse.oys.dto;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.pse.oys.domain.enums.TimeSlot;


/**
 * DTO zur Beantwortung und Bearbeitung des Fragebogens.
 * Enthält alle vom Nutzer angegebenen Lernpräferenzen.
 * Im Prozess der Verarbeitung werden diese Präferenzen auf das
 * ensprechende Format der {@link de.pse.oys.domain.LearningPreferences} gemappt
 * und persistent gespeichert.
 * <p>
 * VERSION 2.0: Angepasste Datentypen für die Präferenzfelder. Da im Frontend die Fragen
 * als Multiple-Choice / Single-Choice - Fragebögen umgesetzt wurden, sind die Felder als Map der gesendeten Antworten
 * mit Boolean-Werten (true/false) realisiert.
 * <p>
 * Die jeweiligen True-Werte sind die relevanten Antworten, die vom Nutzer ausgewählt wurden.
 * Entsprechend geben Getter diese Werte zurück.
 * Die Auswahlmöglichkeiten der Enums sind serverseitig vorgegeben und müssen
 * mit den Keys der Maps übereinstimmen.
 * Die Integer-Werte entsprechen den Auswahlmöglichkeiten auf Client-Seite.
 *<p>
 * Zudem wurde die Json-Property-Naming-Strategy "snake_case" implementiert,
 * sodass Kotlin Konventionen auf Client Seite eingehalten werden können.
 *
 * @author uhupo
 * @version 2.0
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class QuestionnaireDTO {

    private static int DAY_LOAD_LIMIT = 24;

    private static final String ERR_INVALID_QUESTIONNAIRE_DATA = "Ungültige Daten im Fragebogen festgestellt.";

    @JsonProperty("max_unit_duration")
    private Map<Integer, Boolean> maxUnitDuration;

    @JsonProperty("min_unit_duration")
    private Map<Integer, Boolean> minUnitDuration;

    @JsonProperty("max_day_load")
    private Map<Integer, Boolean> maxDayLoad = new java.util.HashMap<>(Map.of(DAY_LOAD_LIMIT, true));//Standardmäßig soll nur die natürliche Tagesbegrenzung vorliegen

    @JsonProperty("time_before_deadlines")
    private Map<Integer, Boolean> timeBeforeDeadlines;

    @JsonProperty("preferred_pause_duration")
    private Map<Integer, Boolean> preferredPauseDuration;

    @JsonProperty("preferred_study_days")
    private Map<DayOfWeek, Boolean> preferredStudyDays;

    @JsonProperty("preferred_study_times")
    private Map<TimeSlot, Boolean> preferredStudyTimes;


    /**
     * Parameterloser Konstruktor für QuestionnaireDTO.
     * Notwendig für das Mapping von JSON-Daten.
     */
    public QuestionnaireDTO() {
        // Standardkonstruktor
    }

// Hilfsmethoden zur Verarbeitung von Single-Choice- und Multiple-Choice-Maps

    /**
     * Gibt den Key mit Wert TRUE aus einer Single-Choice-Map zurück.
     * Der Wert, welcher TRUE ist, ist nach Definition der einzige gültige Wert.
     * @throws InvalidDtoException wenn kein TRUE-Wert gefunden wurde, d.h. die Map ungültig ist.
     * Folglich wurde der Fragebogen fehlerhaft ausgefüllt.
     */
    private static <K> K getSingleChoice(Map<K, Boolean> map) throws InvalidDtoException {
        if (map == null) return null;
        for (Map.Entry<K, Boolean> entry : map.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        throw new InvalidDtoException(ERR_INVALID_QUESTIONNAIRE_DATA);
    }

    /**
     * Setzt alle Werte in einer Single-Choice-Map auf FALSE und den gewünschten Key auf TRUE.
     */
    private static <K> void setSingleChoice(Map<K, Boolean> map, K value) {
        if (map == null) return;
        map.replaceAll((k, v) -> Boolean.FALSE);
        map.put(value, Boolean.TRUE);
    }

    /**
     * Gibt alle Keys mit Wert TRUE aus einer Multiple-Choice-Map als Set zurück.
     * Das sind die Antworten, die vom Nutzer ausgewählt wurden.
     */
    private static <K> Set<K> getMultipleChoice(Map<K, Boolean> map) {
        if (map == null) return java.util.Collections.emptySet();
        return map.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Setzt alle Werte in einer Multiple-Choice-Map auf FALSE und die gewünschten Keys auf TRUE.
     */
    private static <K> void setMultipleChoice(Map<K, Boolean> map, Set<K> values) {
        if (map == null) return;
        map.replaceAll((k, v) -> Boolean.FALSE);
        for (K value : values) {
            map.put(value, Boolean.TRUE);
        }
    }

    // Getter & Setter

    // Single-Choice Felder
    /**
     * Gibt die vom Nutzer gewählte maximale Lerneinheitsdauer zurück.
     * @return Integer-Wert der maximalen Lerneinheitsdauer
     * @throws InvalidDtoException wenn kein gültiger Wert gefunden wurde
     */
    @JsonIgnore
    public Integer getMaxUnitDuration() throws InvalidDtoException {
        return getSingleChoice(this.maxUnitDuration);
    }

    /**
     * Setzt die maximale Lerneinheitsdauer.
     * @param maxUnitDuration Integer-Wert der maximalen Lerneinheitsdauer
     */
    @JsonIgnore
    public void setMaxUnitDuration(Integer maxUnitDuration) {
        if (this.maxUnitDuration == null) {
            this.maxUnitDuration = new java.util.HashMap<>();
        }
        setSingleChoice(this.maxUnitDuration, maxUnitDuration);
    }

    /**
     * Gibt die vom Nutzer gewählte minimale Lerneinheitsdauer zurück.
     * @return Integer-Wert der minimalen Lerneinheitsdauer
     * @throws InvalidDtoException wenn kein gültiger Wert gefunden wurde
     */
    @JsonIgnore
    public Integer getMinUnitDuration() throws InvalidDtoException {
        return getSingleChoice(this.minUnitDuration);
    }

    /**
     * Setzt die minimale Lerneinheitsdauer.
     * @param minUnitDuration Integer-Wert der minimalen Lerneinheitsdauer
     */
    @JsonIgnore
    public void setMinUnitDuration(Integer minUnitDuration) {
        if (this.minUnitDuration == null) {
            this.minUnitDuration = new java.util.HashMap<>();
        }
        setSingleChoice(this.minUnitDuration, minUnitDuration);
    }

    /**
     * Gibt die vom Nutzer gewählte maximale Tagesbelastung zurück.
     * @return Integer-Wert der maximalen Tagesbelastung
     * @throws InvalidDtoException wenn kein gültiger Wert gefunden wurde
     */
    @JsonIgnore
    public Integer getMaxDayLoad() throws InvalidDtoException {
        return getSingleChoice(this.maxDayLoad);
    }

    /**
     * Setzt die maximale Tagesbelastung.
     * @param maxDayLoad Integer-Wert der maximalen Tagesbelastung
     */
    @JsonIgnore
    public void setMaxDayLoad(Integer maxDayLoad) {
        if (this.maxDayLoad == null) {
            this.maxDayLoad = new java.util.HashMap<>();
        }
        setSingleChoice(this.maxDayLoad, maxDayLoad);
    }

    /**
     * Gibt die vom Nutzer gewählte Zeitspanne vor Deadlines zurück.
     * @return Integer-Wert der Zeitspanne vor Deadlines
     */
    @JsonIgnore
    public Integer getTimeBeforeDeadlines() {
        return getSingleChoice(this.timeBeforeDeadlines);
    }

    /**
     * Setzt die Zeitspanne vor Deadlines.
     * @param timeBeforeDeadlines Integer-Wert der Zeitspanne vor Deadlines
     */
    @JsonIgnore
    public void setTimeBeforeDeadlines(Integer timeBeforeDeadlines) {
        if (this.timeBeforeDeadlines == null) {
            this.timeBeforeDeadlines = new java.util.HashMap<>();
        }
        setSingleChoice(this.timeBeforeDeadlines, timeBeforeDeadlines);
    }

    /**
     * Gibt die vom Nutzer bevorzugte Pausendauer zurück.
     * @return Integer-Wert der bevorzugten Pausendauer
     * @throws IllegalStateException wenn kein gültiger Wert gefunden wurde
     */
    @JsonIgnore
    public Integer getPreferredPauseDuration() throws IllegalStateException {
        return getSingleChoice(this.preferredPauseDuration);
    }

    /**
     * Setzt die bevorzugte Pausendauer.
     * @param preferredPauseDuration Integer-Wert der bevorzugten Pausendauer
     */
    @JsonIgnore
    public void setPreferredPauseDuration(Integer preferredPauseDuration) {
        if (this.preferredPauseDuration == null) {
            this.preferredPauseDuration = new java.util.HashMap<>();
        }
        setSingleChoice(this.preferredPauseDuration, preferredPauseDuration);
    }

    // Multiple-Choice Felder

    /**
     * Gibt die vom Nutzer bevorzugten Lernzeiten als Set zurück.
     * @return Set der ausgewählten {@link TimeSlot}-Werte
     */
    @JsonIgnore
    public Set<TimeSlot> getPreferredStudyTimes() {
        return getMultipleChoice(this.preferredStudyTimes);
    }

    /**
     * Setzt die bevorzugten Lernzeiten.
     * @param preferredStudyTimes Set der ausgewählten {@link TimeSlot}-Werte
     */
    @JsonIgnore
    public void setPreferredStudyTimes(Set<TimeSlot> preferredStudyTimes) {
        if (this.preferredStudyTimes == null) {
            this.preferredStudyTimes = new java.util.EnumMap<>(TimeSlot.class);
        }
        setMultipleChoice(this.preferredStudyTimes, preferredStudyTimes);
    }

    /**
     * Gibt die vom Nutzer bevorzugten Lerntage als Set zurück.
     * @return Set der ausgewählten {@link DayOfWeek}-Werte
     */
    @JsonIgnore
    public Set<DayOfWeek> getPreferredStudyDays() {
        return getMultipleChoice(this.preferredStudyDays);
    }

    /**
     * Setzt die bevorzugten Lerntage.
     * @param preferredStudyDays Set der ausgewählten {@link DayOfWeek}-Werte
     */
    @JsonIgnore
    public void setPreferredStudyDays(Set<DayOfWeek> preferredStudyDays) {
        if (this.preferredStudyDays == null) {
            this.preferredStudyDays = new java.util.EnumMap<>(DayOfWeek.class);
        }
        setMultipleChoice(this.preferredStudyDays, preferredStudyDays);
    }
}
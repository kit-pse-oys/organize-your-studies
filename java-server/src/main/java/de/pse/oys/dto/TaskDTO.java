package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Gemeinsame Basis für Task-DTOs.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category",
        visible = true // Erlaubt, dass das Feld auch im Enum-Feld landet
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExamTaskDTO.class, name = "exam"),
        @JsonSubTypes.Type(value = SubmissionTaskDTO.class, name = "submission"),
        @JsonSubTypes.Type(value = OtherTaskDTO.class, name = "other")
})
public abstract class TaskDTO {

    private String title;
    private String moduleTitle;
    private TaskCategory category;
    private Integer weeklyTimeLoad;

    /**
     * Default-Konstruktor (z.B. für Deserialisierung).
     */
    protected TaskDTO() {
    }

    /**
     * Erstellt ein TaskDTO mit gemeinsamen Basisfeldern.
     *
     * @param title            Titel der Aufgabe
     * @param moduleTitle      Titel des zugehörigen Moduls
     * @param category         Kategorie der Aufgabe
     * @param weeklyTimeLoad   wöchentlicher Zeitaufwand (z.B. in Minuten)
     */
    protected TaskDTO(String title,
                      String moduleTitle,
                      TaskCategory category,
                      Integer weeklyTimeLoad) {
        this.title = title;
        this.moduleTitle = moduleTitle;
        this.category = category;
        this.weeklyTimeLoad = weeklyTimeLoad;
    }

    /**
     * Liefert den Titel der Aufgabe.
     *
     * @return Titel der Aufgabe
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setzt den Titel der Aufgabe.
     *
     * @param title Titel der Aufgabe
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Liefert den Titel des zugehörigen Moduls.
     *
     * @return Modultitel
     */
    public String getModuleTitle() {
        return moduleTitle;
    }

    /**
     * Setzt den Titel des zugehörigen Moduls.
     *
     * @param moduleTitle Modultitel
     */
    public void setModuleTitle(String moduleTitle) {
        this.moduleTitle = moduleTitle;
    }

    /**
     * Liefert die Kategorie der Aufgabe.
     *
     * @return Kategorie
     */
    public TaskCategory getCategory() {
        return category;
    }

    /**
     * Setzt die Kategorie der Aufgabe.
     *
     * @param category Kategorie
     */
    public void setCategory(TaskCategory category) {
        this.category = category;
    }

    /**
     * Liefert den wöchentlichen Zeitaufwand.
     *
     * @return wöchentlicher Zeitaufwand (z.B. in Minuten)
     */
    public Integer getWeeklyTimeLoad() {
        return weeklyTimeLoad;
    }

    /**
     * Setzt den wöchentlichen Zeitaufwand.
     *
     * @param weeklyTimeLoad wöchentlicher Zeitaufwand (z.B. in Minuten)
     */
    public void setWeeklyTimeLoad(Integer weeklyTimeLoad) {
        this.weeklyTimeLoad = weeklyTimeLoad;
    }
}

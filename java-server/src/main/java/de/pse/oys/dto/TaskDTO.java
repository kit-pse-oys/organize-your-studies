package de.pse.oys.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.pse.oys.domain.enums.TaskCategory;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;


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
        @JsonSubTypes.Type(value = ExamTaskDTO.class, name = "EXAM"),
        @JsonSubTypes.Type(value = SubmissionTaskDTO.class, name = "SUBMISSION"),
        @JsonSubTypes.Type(value = OtherTaskDTO.class, name = "OTHER")
})
public abstract class TaskDTO {

    private String title;
    @JsonProperty("module")
    private UUID moduleId;
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
     * @param moduleId      Id des zugehörigen Moduls
     * @param category         Kategorie der Aufgabe
     * @param weeklyTimeLoad   wöchentlicher Zeitaufwand (z.B. in Minuten)
     */
    protected TaskDTO(String title,
                      UUID moduleId,
                      TaskCategory category,
                      Integer weeklyTimeLoad) {
        this.title = title;
        this.moduleId = moduleId;
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
     * Liefert die UUID des zugehörigen Moduls.
     *
     * @return die UUID des Moduls
     */
    public UUID getModuleId() {
        return moduleId;
    }

    /**
     * Setzt die Id des zugehörigen Moduls.
     *
     * @param moduleId ModulId
     */
    public void setModuleId(UUID moduleId) {
        this.moduleId = moduleId;
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

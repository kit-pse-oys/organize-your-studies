package de.pse.oys.dto.response;

import de.pse.oys.dto.TaskDTO;

import java.util.List;
import java.util.UUID;

/**
 * Datentransferobjekt für die Antwort eines Moduls an das Frontend.
 * Enthält die Stammdaten des Moduls sowie die zugehörigen Aufgaben.
 * @author utgid
 * @version 1.0
 */
public class ModuleResponseDTO {

    /** Eindeutige Kennung des Moduls. */
    private UUID id;

    /** Der Name des Moduls. */
    private String titel;

    /** Eine kurze Beschreibung der Modulinhalte. */
    private String description;

    /** Der hexadezimale Farbcode zur Darstellung im UI. */
    private String color;

    /** Die Liste der Aufgaben, die diesem Modul zugeordnet sind. */
    private List<TaskDTO> tasks;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public ModuleResponseDTO() {
    }

    // Getter

    /** @return Die ID des Moduls. */
    public UUID getId() { return id; }

    /** @return Der Titel des Moduls. */
    public String getTitel() { return titel; }

    /** @return Die Beschreibung des Moduls. */
    public String getDescription() { return description; }

    /** @return Der Farbcode des Moduls. */
    public String getColor() { return color; }

    /** @return Die Liste der zugehörigen Aufgaben. */
    public List<TaskDTO> getTasks() { return tasks; }

    // Setter

    /** @param id Die eindeutige Kennung des Moduls. */
    public void setId(UUID id) { this.id = id; }

    /** @param titel Der Name des Moduls. */
    public void setTitel(String titel) { this.titel = titel; }

    /** @param description Die Modulbeschreibung. */
    public void setDescription(String description) { this.description = description; }

    /** @param color Der Hex-Farbcode für die UI. */
    public void setColor(String color) { this.color = color; }

    /** @param tasks Die Liste der Aufgaben für dieses Modul. */
    public void setTasks(List<TaskDTO> tasks) { this.tasks = tasks; }
}
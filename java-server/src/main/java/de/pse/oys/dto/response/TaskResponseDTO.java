package de.pse.oys.dto.response;

import de.pse.oys.domain.enums.TaskCategory;

import java.util.Map;
import java.util.UUID;

/**
 * Datentransferobjekt für die Übermittlung von Aufgabendaten an das Frontend.
 * Die Struktur folgt strikt der Spezifikation aus Tabelle 3.4 des Entwurfshefts.
 * @author utgid
 * @version 1.0
 */
public class TaskResponseDTO {

    /** Eindeutige Kennung der Aufgabe. */
    private UUID id;

    /** Der Titel der Aufgabe. */
    private String title;

    /** Die fachliche Kategorie (z. B. "EXAM", "SUBMISSION"). */
    private TaskCategory type;

    /** Der Name des zugehörigen Moduls. */
    private String module;

    /** Status, ob die Aufgabe bereits abgeschlossen wurde. */
    private boolean isCompleted;

    /**
     * Zusätzliche typspezifische Felder (z. B. deadlines oder timeframes),
     * flexibel als Map abgebildet.
     */
    private Map<String, Object> extraFields;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public TaskResponseDTO() {
    }

    // Getter

    /** @return Die ID der Aufgabe. */
    public UUID getId() { return id; }

    /** @return Der Titel der Aufgabe. */
    public String getTitle() { return title; }

    /** @return Der Typ der Aufgabe. */
    public TaskCategory getType() { return type; }

    /** @return Der Modulname. */
    public String getModule() { return module; }

    /** @return true, wenn die Aufgabe abgeschlossen ist. */
    public boolean isCompleted() { return isCompleted; }

    /** @return Map mit zusätzlichen Feldern. */
    public Map<String, Object> getExtraFields() { return extraFields; }

    // Setter

    /** @param id Die eindeutige Kennung. */
    public void setId(UUID id) { this.id = id; }

    /** @param title Der Aufgabenname. */
    public void setTitle(String title) { this.title = title; }

    /** @param type Der Aufgabentyp. */
    public void setType(TaskCategory type) { this.type = type; }

    /** @param module Das zugehörige Modul. */
    public void setModule(String module) { this.module = module; }

    /** @param isCompleted Der Abschlussstatus. */
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }

    /** @param extraFields Die typspezifischen Zusatzdaten. */
    public void setExtraFields(Map<String, Object> extraFields) { this.extraFields = extraFields; }
}
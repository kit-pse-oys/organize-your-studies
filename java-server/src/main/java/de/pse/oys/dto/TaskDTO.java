package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;

/**
 * Gemeinsame Basis für Task-DTOs.
 */
public abstract class TaskDTO {

    private String title;
    private String moduleTitle;
    private TaskCategory category;
    private Integer weeklyTimeLoad;
    private boolean sendNotification;

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
     * @param sendNotification ob Benachrichtigungen gesendet werden sollen
     */
    protected TaskDTO(String title,
                      String moduleTitle,
                      TaskCategory category,
                      Integer weeklyTimeLoad,
                      boolean sendNotification) {
        this.title = title;
        this.moduleTitle = moduleTitle;
        this.category = category;
        this.weeklyTimeLoad = weeklyTimeLoad;
        this.sendNotification = sendNotification;
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

    /**
     * Gibt an, ob Benachrichtigungen gesendet werden sollen.
     *
     * @return true, wenn Benachrichtigungen aktiv sind, sonst false
     */
    public boolean isSendNotification() {
        return sendNotification;
    }

    /**
     * Setzt, ob Benachrichtigungen gesendet werden sollen.
     *
     * @param sendNotification true, wenn Benachrichtigungen aktiv sein sollen
     */
    public void setSendNotification(boolean sendNotification) {
        this.sendNotification = sendNotification;
    }
}

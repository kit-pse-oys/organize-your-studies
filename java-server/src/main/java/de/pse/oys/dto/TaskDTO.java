package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;

/**
 * Data Transfer Object für Aufgaben (Tasks).
 * Enthält alle relevanten Informationen zur Kommunikation zwischen Client und Server.
 */
public class TaskDTO {

    private String title;
    private String moduleTitle;
    private TaskCategory category;
    private Integer weeklyTimeLoad;
    private boolean sendNotification;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public TaskDTO() {}

    // Getter und Setter

    /**
     * Getter für den Titel der Aufgabe.
     * @return Der Titel der Aufgabe.
     */
    public String getTitle() { return title; }

    /**
     * Setter für den Titel der Aufgabe.
     * @param title Der Titel der Aufgabe.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Getter für den Titel des Moduls.
     * @return Der Titel des Moduls.
     */
    public String getModuleTitle() { return moduleTitle; }

    /**
     * Setter für den Titel des Moduls.
     * @param moduleTitle Der Titel des Moduls.
     */
    public void setModuleTitle(String moduleTitle) { this.moduleTitle = moduleTitle; }

    /**
     * Getter für die Kategorie der Aufgabe.
     * @return Die Kategorie der Aufgabe.
     */
    public TaskCategory getCategory() { return category; }

    /**
     * Setter für die Kategorie der Aufgabe.
     * @param category Die Kategorie der Aufgabe.
     */
    public void setCategory(TaskCategory category) { this.category = category; }

    /**
     * Getter für die wöchentliche Zeitbelastung.
     * @return Die wöchentliche Zeitbelastung.
     */
    public Integer getWeeklyTimeLoad() { return weeklyTimeLoad; }

    /**
     * Setter für die wöchentliche Zeitbelastung.
     * @param weeklyTimeLoad Die wöchentliche Zeitbelastung.
     */
    public void setWeeklyTimeLoad(Integer weeklyTimeLoad) { this.weeklyTimeLoad = weeklyTimeLoad; }

    /**
     * Gibt an, ob eine Benachrichtigung gesendet werden soll.
     * @return true, wenn eine Benachrichtigung gesendet werden soll, sonst false.
     */
    public boolean isSendNotification() { return sendNotification; }

    /**
     * Legt fest, ob eine Benachrichtigung gesendet werden soll.
     * @param sendNotification true, wenn eine Benachrichtigung gesendet werden soll, sonst false.
     */
    public void setSendNotification(boolean sendNotification) { this.sendNotification = sendNotification; }
}
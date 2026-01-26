package de.pse.oys.dto;

import de.pse.oys.domain.enums.TaskCategory;
import java.util.UUID;

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModuleTitle() { return moduleTitle; }
    public void setModuleTitle(String moduleTitle) { this.moduleTitle = moduleTitle; }

    public TaskCategory getCategory() { return category; }
    public void setCategory(TaskCategory category) { this.category = category; }

    public Integer getWeeklyTimeLoad() { return weeklyTimeLoad; }
    public void setWeeklyTimeLoad(Integer weeklyTimeLoad) { this.weeklyTimeLoad = weeklyTimeLoad; }

    public boolean isSendNotification() { return sendNotification; }
    public void setSendNotification(boolean sendNotification) { this.sendNotification = sendNotification; }
}
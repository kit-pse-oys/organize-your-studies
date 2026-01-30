package de.pse.oys.dto;

import de.pse.oys.domain.enums.ModulePriority;

import java.util.UUID;

/**
 * Datentransferobjekt für die Erstellung und Verwaltung von Modulen.
 * Dieses DTO enthält die grundlegenden Konfigurationsdaten eines Moduls.
 */
public class ModuleDTO {

    ///** Die eindeutige Kennung des Moduls. */
    private UUID id;

    /** Der Titel des Moduls. */
    private String title;

    /** Eine ausführliche Beschreibung des Moduls. */
    private String description;

    /** Die Priorität des Moduls für den Planungsalgorithmus. */
    private ModulePriority priority;

    /** Der hexadezimale Farbcode für die Darstellung im UI. */
    private String color;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public ModuleDTO() {
        // Standardkonstruktor
    }

    // Getter

    /** @return Die eindeutige ID des Moduls. */
    public UUID getId() { return id; }

    /** @return Der Titel des Moduls. */
    public String getTitle() { return title; }

    /** @return Die Beschreibung des Moduls. */
    public String getDescription() { return description; }

    /** @return Die Priorität des Moduls. */
    public ModulePriority getPriority() { return priority; }

    /** @return Der Farbcode (HEX). */
    public String getColor() { return color; }

    // Setter

    /** @param id Die zu setzende Modul-ID. */
    public void setId(UUID id) { this.id = id; }

    /** @param title Der zu setzende Modulname. */
    public void setTitle(String title) { this.title = title; }

    /** @param description Die Modulbeschreibung. */
    public void setDescription(String description) { this.description = description; }

    /** @param priority Die Wichtigkeit des Moduls. */
    public void setPriority(ModulePriority priority) { this.priority = priority; }

    /** @param color Der Hex-Farbcode für das UI. */
    public void setColor(String color) { this.color = color; }
}
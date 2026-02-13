package de.pse.oys.domain;

import de.pse.oys.domain.enums.ModulePriority;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repräsentiert ein Studienmodul (z. B. eine Vorlesung), das vom Nutzer verwaltet wird.
 * Ein Modul dient als Container für verschiedene Aufgaben (Tasks) und definiert
 * grundlegende Eigenschaften wie Farbe und Priorität.
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "modules")
public class Module {

    /** Eindeutige Kennung des Moduls (readOnly). */
    @Id
    @GeneratedValue
    @Column(name = "moduleid", updatable = false)
    private UUID moduleId;

    /** Der Titel des Moduls (z. B. "Programmieren"). */
    @Column(name = "title", nullable = false)
    private String title;

    /** Eine optionale nähere Beschreibung des Modulinhalts. */
    @Column(name = "description")
    private String description;

    /** Farbe zur visuellen Kennzeichnung im Kalender (HEX-Code). */
    @Column(name = "color_hex_code")
    private String colorHexCode;

    /** Die Priorität des Moduls für die Planung. */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ModulePriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Wir nennen die Spalte explizit user_id
    private User user;

    /**
     * Liste der Aufgaben, die diesem Modul untergeordnet sind.
     * cascade = ALL stellt sicher, dass Aufgaben bei Modullöschung mitentfernt werden.
     */
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected Module() {
    }

    /**
     * Erzeugt ein neues Modul mit den erforderlichen Grunddaten.
     *
     * @param title    Name des Moduls.
     * @param priority Wichtigkeit des Moduls.
     */
    public Module(String title, ModulePriority priority) {
        this.title = title;
        this.priority = priority;
    }


    /**
     * Fügt eine neue Aufgabe zu diesem Modul hinzu und stellt die
     * bidirektionale Konsistenz sicher.
     * * @param task Die hinzuzufügende Aufgabe.
     */
    public void addTask(Task task) {
        if (task != null && !this.tasks.contains(task)) {
            this.tasks.add(task);
            task.setModule(this);
        }
    }

    /**
     * Entfernt eine Aufgabe aus diesem Modul und löst die
     * bidirektionale Verknüpfung auf.
     * * @param task Die zu entfernende Aufgabe.
     */
    public void deleteTask(Task task) {
        if (task != null && this.tasks.contains(task)) {
            this.tasks.remove(task);
            task.setModule(null);
        }
    }

    // Getter

    /** @return Die ID des Moduls. */
    public UUID getModuleId() {
        return moduleId;
    }

    /** @return Der Titel des Moduls. */
    public String getTitle() {
        return title;
    }

    /** @return Die Priorität des Moduls. */
    public ModulePriority getPriority() {
        return priority;
    }

    /** @return Die Liste der zugehörigen Aufgaben. */
    public List<Task> getTasks() {
        return tasks;
    }

    /** @return Die Beschreibung des Moduls. */
    public String getDescription() {
        return description;
    }

    /** @return Der Farbcode des Moduls. */
    public String getColorHexCode() {
        return colorHexCode;
    }

    // Setter

    /** @param title Der neue Titel des Moduls. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @param description Die neue Beschreibung. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @param colorHexCode Der neue Farbcode für die Anzeige. */
    public void setColorHexCode(String colorHexCode) {
        this.colorHexCode = colorHexCode;
    }

    /** @param priority Die neue Prioritätsstufe. */
    public void setPriority(ModulePriority priority) {
        this.priority = priority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
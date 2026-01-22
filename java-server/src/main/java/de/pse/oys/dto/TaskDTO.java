package de.pse.oys.dto;

import java.util.List;

/**
 * Data Transfer Object (DTO) für Aufgaben im Lernplan.
 */
public class TaskDTO {
    private int id;
    private int duration;
    private int deadline;
    private List<CostDTO> costs;
    /** Konstruktor für TaskDTO.
     *
     * @param id       Eindeutige ID der Aufgabe.
     * @param duration Dauer der Aufgabe.
     * @param deadline Abgabefrist der Aufgabe.
     * @param costs    Kosteninformationen der Aufgabe.
     */
    public TaskDTO(int id, int duration, int deadline, List<CostDTO> costs) {
        this.id = id;
        this.duration = duration;
        this.deadline = deadline;
        this.costs = costs;
    }
    /** @return Eindeutige ID der Aufgabe. */
    public int getId() {
        return id;
    }
    /** @return Dauer der Aufgabe. */
    public int getDuration() {
        return duration;
    }
    /** @return Abgabefrist der Aufgabe. */
    public int getDeadline() {
        return deadline;
    }
    /** @return Kosteninformationen der Aufgabe. */
    public List<CostDTO> getCosts() {
        return costs;
    }

}

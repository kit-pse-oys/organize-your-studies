package de.pse.oys.dto.plan;

import de.pse.oys.dto.CostDTO;

import java.util.List;

/**
 * Data Transfer Object (DTO) für Aufgaben im Lernplan.
 */
public class PlanningTaskDTO {
    private String id;
    private int duration;
    private int start;
    private int deadline;
    private List<CostDTO> costs;
    /** Konstruktor für TaskDTO.
     *
     * @param id       Eindeutige ID der Aufgabe.
     * @param duration Dauer der Aufgabe.
     * @param deadline Abgabefrist der Aufgabe.
     * @param costs    Kosteninformationen der Aufgabe.
     */
    public PlanningTaskDTO(String id, int duration, int start, int deadline, List<CostDTO> costs) {
        this.id = id;
        this.duration = duration;
        this.start = start;
        this.deadline = deadline;
        this.costs = costs;
    }
    /** @return Eindeutige ID der Aufgabe. */
    public String getId() {

        return id;
    }
    /** @return Dauer der Aufgabe. */
    public int getDuration() {

        return duration;
    }
    /** @return Startzeit der Aufgabe. */
    public int getStart() {
        return start;
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

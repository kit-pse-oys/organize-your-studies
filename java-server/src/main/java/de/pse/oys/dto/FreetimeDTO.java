package de.pse.oys.dto;
/**
 * Data Transfer Object (DTO) für freie Zeitblöcke im Lernplan.
 */
public class FreetimeDTO {
    private int start;
    private int duration;

/** Konstruktor für FreetimeDTO.
     *
     * @param start    Startzeitpunkt des freien Zeitblocks.
     * @param duration Dauer des freien Zeitblocks.
     */
    public FreetimeDTO(int start, int duration) {
        this.start = start;
        this.duration = duration;
    }


    /** @return Startzeitpunkt des freien Zeitblocks. */

    public int getStart() {
        return start;
    }
    /** @return Dauer des freien Zeitblocks. */
    public int getDuration() {
        return duration;
    }
}

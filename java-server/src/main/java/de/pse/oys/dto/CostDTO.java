package de.pse.oys.dto;
/**
 * Data Transfer Object (DTO) für Kosteninformationen im Lernplan.
 */
public class CostDTO {
    private int t;
    private int c;
    /** Konstruktor für CostDTO.
     *
     * @param t Zeitkomponente der Kosten.
     * @param c Kostenwert.
     */
    public CostDTO(int t, int c) {
        this.t = t;
        this.c = c;
    }
    /*** Standardkonstruktor für CostDTO. */
    public CostDTO(){
        // Standardkonstruktor
    }
/** @return Kostenwert. */
    public int getC() {
        return c;
    }
/** @return Zeitkomponente der Kosten. */
    public int getT() {
        return t;
    }

    /**
     * Setzt die Zeitkomponente der Kosten.
     * @param t Die neue Zeitkomponente der Kosten.
     */
    public void setT(int t) {
        this.t = t;
    }

    /**
     * Setzt den Kostenwert.
     * @param c Der neue Kostenwert.
     */
    public void setC(int c) {
        this.c = c;
    }

}

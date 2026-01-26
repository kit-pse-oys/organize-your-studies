package de.pse.oys.domain.enums;

/**
 * Beschreibt die subjektiv wahrgenommene Dauer einer Lerneinheit im Vergleich
 * zur tatsächlich geplanten Zeit. Dies dient der Optimierung zukünftiger Zeitfenster.
 * <p>
 * Jedem Wert ist ein Anpassungsfaktor zugeordnet, der bei der Neuberechnung
 * der geplanten Dauer berücksichtigt werden kann.
 */
public enum PerceivedDuration {

    /** Die geplante Zeit war viel zu kurz für den Stoff. */
    MUCH_TOO_SHORT(0.25),

    /** Die Zeit war eher knapp bemessen. */
    TOO_SHORT(0.125),

    /** Die geplante Zeit war genau passend. */
    IDEAL(0.0),

    /** Die Einheit fühlte sich länger an als nötig. */
    TOO_LONG(-0.125),

    /** Die geplante Zeit war viel zu großzügig bemessen. */
    MUCH_TOO_LONG(-0.25);


    private final double adjustmentFactor;
    PerceivedDuration(double adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }
    public double getAdjustmentFactor() {
        return adjustmentFactor;
    }
}
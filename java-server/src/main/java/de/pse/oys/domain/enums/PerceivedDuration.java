package de.pse.oys.domain.enums;

/**
 * Beschreibt die subjektiv wahrgenommene Dauer einer Lerneinheit im Vergleich
 * zur tatsächlich geplanten Zeit. Dies dient der Optimierung zukünftiger Zeitfenster.
 *
 * @author uhupo
 * @version 2.0
 * Neu in Version 2.0: Hinzugefügte Anpassungswerte für jede Wahrnehmungskategorie, welche konkret dem
 * Enum zugeordnet sind.
 */
public enum PerceivedDuration {

    /** Die geplante Zeit war viel zu kurz für den Stoff. */
    MUCH_TOO_SHORT(2.5),

    /** Die Zeit war eher knapp bemessen. */
    TOO_SHORT(1.25),

    /** Die geplante Zeit war genau passend. */
    IDEAL(0.0),

    /** Die Einheit fühlte sich länger an als nötig. */
    TOO_LONG(-1.25),

    /** Die geplante Zeit war viel zu großzügig bemessen. */
    MUCH_TOO_LONG(-2.5);


    private final double adjustmentValue;

    PerceivedDuration(double value) {
        this.adjustmentValue = value;
    }

    /**
     * Liefert den Anpassungsfaktor basierend auf der wahrgenommenen Dauer.
     * Dieser Wert kann verwendet werden, um zukünftige Zeitpläne zu optimieren.
     * @see de.pse.oys.service.planning.PlanningService
     * @return Ein double-Wert, der den Anpassungsfaktor repräsentiert.
     */
    public double getAdjustmentValue() {
        return this.adjustmentValue;
    }
}
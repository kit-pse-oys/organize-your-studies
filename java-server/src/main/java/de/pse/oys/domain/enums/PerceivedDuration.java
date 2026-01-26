package de.pse.oys.domain.enums;

/**
 * Beschreibt die subjektiv wahrgenommene Dauer einer Lerneinheit im Vergleich
 * zur tatsächlich geplanten Zeit. Dies dient der Optimierung zukünftiger Zeitfenster.
 */
public enum PerceivedDuration {

    /** Die geplante Zeit war viel zu kurz für den Stoff. */
    MUCH_TOO_SHORT,

    /** Die Zeit war eher knapp bemessen. */
    TOO_SHORT,

    /** Die geplante Zeit war genau passend. */
    IDEAL,

    /** Die Einheit fühlte sich länger an als nötig. */
    TOO_LONG,

    /** Die geplante Zeit war viel zu großzügig bemessen. */
    MUCH_TOO_LONG
}
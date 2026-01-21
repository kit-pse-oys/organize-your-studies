package de.pse.oys.domain.enums;

/**
 * Definiert den Grad der Zielerreichung nach Abschluss einer Lerneinheit.
 * Gibt an, inwieweit die für diese Einheit gesetzten Lernziele erreicht wurden.
 */
public enum AchievementLevel {

    /** Es wurde kein Fortschritt erzielt. */
    NONE,

    /** Nur sehr geringe Teile des Lernstoffs wurden verarbeitet. */
    POOR,

    /** Ungefähr die Hälfte der Ziele wurde erreicht. */
    PARTIAL,

    /** Die meisten Lernziele wurden erfolgreich umgesetzt. */
    GOOD,

    /** Alle gesetzten Ziele wurden vollständig oder übertroffen erreicht. */
    EXCELLENT
}
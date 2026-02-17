package de.pse.oys.domain.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Definiert den Grad der Zielerreichung nach Abschluss einer Lerneinheit.
 * Gibt an, inwieweit die für diese Einheit gesetzten Lernziele erreicht wurden.
 *
 * @author utgid
 * @version 1.0
 */
public enum AchievementLevel {

    /** Es wurde kein Fortschritt erzielt. */
    @JsonProperty("LOWEST")
    NONE,

    /** Nur sehr geringe Teile des Lernstoffs wurden verarbeitet. */
    @JsonProperty("LOW")
    POOR,

    /** Ungefähr die Hälfte der Ziele wurde erreicht. */
    @JsonProperty("MEDIUM")
    PARTIAL,

    /** Die meisten Lernziele wurden erfolgreich umgesetzt. */
    @JsonProperty("HIGH")
    GOOD,

    /** Alle gesetzten Ziele wurden vollständig oder übertroffen erreicht. */
    @JsonProperty("HIGHEST")
    EXCELLENT
}
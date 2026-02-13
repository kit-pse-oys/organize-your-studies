package de.pse.oys.domain.enums;

/**
 * Definiert die Prioritätsstufen für ein Studienmodul.
 * Die Priorität beeinflusst die Gewichtung der zugehörigen Aufgaben
 * bei der automatischen Lernplangenerierung.
 *
 * @author utgid
 * @version 1.0
 */
public enum ModulePriority {
    /** Niedrige Priorität für weniger wichtige Module. */
    LOW,
    /** Standardmäßige Priorität. */
    MEDIUM,
    /** Hohe Priorität für Kernmodule oder Module mit hohem Aufwand. */
    HIGH
}
package de.pse.oys.domain.enums;

/**
 * Repräsentiert den aktuellen Bearbeitungsstatus einer Aufgabe.
 * @author utgid
 * @version 1.0
 */
public enum TaskStatus {
    /** Aufgabe wurde erstellt, ist aktiv, aber noch nicht abgeschlossen. */
    OPEN,
    /** Aufgabe wurde erfolgreich abgeschlossen, oder hat noch nicht angefangen. */
    CLOSED
}
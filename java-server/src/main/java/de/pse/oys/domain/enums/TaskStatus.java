package de.pse.oys.domain.enums;

/**
 * Repräsentiert den aktuellen Bearbeitungsstatus einer Aufgabe.
 */
public enum TaskStatus {
    /** Aufgabe wurde erstellt, aber noch nicht begonnen. */
    OPEN,
    /** Aufgabe befindet sich in Bearbeitung. */
    IN_PROGRESS,
    /** Aufgabe wurde erfolgreich abgeschlossen. */
    COMPLETED
}
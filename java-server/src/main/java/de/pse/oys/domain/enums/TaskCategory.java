package de.pse.oys.domain.enums;

/**
 * Definiert die fachlichen Kategorien einer Aufgabe.
 * @author utgid
 * @version 1.0
 */
public enum TaskCategory {
    /** Aufgaben mit festem Abgabetermin. */
    SUBMISSION,
    /** Aufgaben, die auf eine Prüfung vorbereiten. */
    EXAM,
    /** Allgemeine Aufgaben ohne strikte Deadline. */
    OTHER
}

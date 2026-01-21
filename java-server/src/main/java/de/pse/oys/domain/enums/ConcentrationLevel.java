package de.pse.oys.domain.enums;

/**
 * Definiert die Stufen der Konzentrationsfähigkeit während einer Lerneinheit.
 * Diese subjektive Metrik hilft dem System, die Effektivität von Lernphasen zu bewerten.
 */
public enum ConcentrationLevel {

    /** Sehr niedrige Konzentration, starke Ablenkung. */
    VERY_LOW,

    /** Niedrige Konzentration. */
    LOW,

    /** Durchschnittliche Konzentration. */
    MEDIUM,

    /** Hohe Konzentration, fokussiertes Arbeiten. */
    HIGH,

    /** Sehr hohe Konzentration, Zustand des "Flows". */
    VERY_HIGH
}
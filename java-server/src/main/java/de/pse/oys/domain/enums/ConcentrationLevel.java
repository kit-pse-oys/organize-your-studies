package de.pse.oys.domain.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Definiert die Stufen der Konzentrationsfähigkeit während einer Lerneinheit.
 * Diese subjektive Metrik hilft dem System, die Effektivität von Lernphasen zu bewerten.
 *
 * @author utgid
 * @version 1.0
 */
public enum ConcentrationLevel {

    /** Sehr niedrige Konzentration, starke Ablenkung. */
    @JsonProperty("LOWEST")
    VERY_LOW,

    /** Niedrige Konzentration. */
    @JsonProperty("LOW")
    LOW,

    /** Durchschnittliche Konzentration. */
    @JsonProperty("MEDIUM")
    MEDIUM,

    /** Hohe Konzentration, fokussiertes Arbeiten. */
    @JsonProperty("HIGH")
    HIGH,

    /** Sehr hohe Konzentration, Zustand des "Flows". */
    @JsonProperty("HIGHEST")
    VERY_HIGH
}
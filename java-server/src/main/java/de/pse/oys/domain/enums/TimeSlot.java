package de.pse.oys.domain.enums;

/**
 * Definiert die zeitlichen Grenzen der Tagesabschnitte für die Lernplanung.
 * Diese Intervalle dienen dem Planungsalgorithmus als Grundlage, um die
 * Präferenzen des Nutzers mit den tatsächlichen Zeitslots im Kalender abzugleichen.
 * @author utgid
 * @version 1.0
 */
public enum TimeSlot {

    /**
     * Der frühe Morgenabschnitt.
     * Zeitrahmen: 06:00 Uhr bis 09:00 Uhr.
     */
    MORNING,

    /**
     * Der klassische Vormittagsabschnitt.
     * Zeitrahmen: 09:00 Uhr bis 12:00 Uhr.
     */
    FORENOON,

    /**
     * Der Mittagsabschnitt.
     * Zeitrahmen: 12:00 Uhr bis 14:00 Uhr.
     */
    NOON,

    /**
     * Der Nachmittagsabschnitt.
     * Zeitrahmen: 14:00 Uhr bis 17:00 Uhr.
     */
    AFTERNOON,

    /**
     * Der Abendabschnitt.
     * Zeitrahmen: 17:00 Uhr bis 21:00 Uhr.
     */
    EVENING
}
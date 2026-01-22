package de.pse.oys.domain.enums;

/**
 * Repräsentiert den aktuellen Status einer Lerneinheit im Lernplan.
 * Dieser Status gibt Auskunft darüber, ob eine Einheit noch bevorsteht,
 * erfolgreich abgeschlossen wurde oder nicht wahrgenommen werden konnte.
 */
public enum UnitStatus {

    /** Die Lerneinheit ist für die Zukunft geplant. */
    PLANNED,

    /** Die Lerneinheit wurde vom Nutzer erfolgreich durchgeführt. */
    COMPLETED,

    /** Der geplante Zeitraum der Lerneinheit ist ohne Bearbeitung verstrichen. */
    MISSED
}
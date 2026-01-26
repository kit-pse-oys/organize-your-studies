package de.pse.oys.dto.response;

import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.UnitDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Datentransferobjekt für die Übermittlung eines Lernplans an das Frontend.
 * Die Struktur folgt strikt der Spezifikation aus Tabelle 3.5 des Entwurfshefts
 * unter Verwendung der vereinbarten DTO-Namen für Einheiten und Freizeit.
 */
public class LearningPlanDTO {

    /** Eindeutige Kennung des Lernplans. */
    private UUID id;

    /** Das Datum, ab dem der Plan gültig ist (Wochenstart). */
    private LocalDate validFrom;

    /** Das Datum, bis zu dem der Plan gültig ist (Wochenende). */
    private LocalDate validUntil;

    /**
     * Liste der im Plan enthaltenen Lerneinheiten.
     * Nutzt das UnitDTO, welches separat implementiert wird.
     */
    private List<UnitDTO> units;

    /**
     * Liste der für diesen Zeitraum verfügbaren Freizeitfenster (Slots).
     * Nutzt das FreeTimeDTO, welches separat implementiert wird.
     */
    private List<FreeTimeDTO> availableSlots;

    /**
     * Standardkonstruktor für die JSON-Deserialisierung.
     */
    public LearningPlanDTO() {
    }

    // Getter

    /** @return Die ID des Lernplans. */
    public UUID getId() { return id; }

    /** @return Der Beginn des Gültigkeitszeitraums. */
    public LocalDate getValidFrom() { return validFrom; }

    /** @return Das Ende des Gültigkeitszeitraums. */
    public LocalDate getValidUntil() { return validUntil; }

    /** @return Die Liste der Lerneinheiten im Plan. */
    public List<UnitDTO> getUnits() { return units; }

    /** @return Die verfügbaren Freizeit-Slots. */
    public List<FreeTimeDTO> getAvailableSlots() { return availableSlots; }

    // Setter

    /** @param id Die eindeutige Kennung des Plans. */
    public void setId(UUID id) { this.id = id; }

    /** @param validFrom Das Startdatum der Woche. */
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    /** @param validUntil Das Enddatum der Woche. */
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    /** @param units Die Liste der Lerneinheiten (UnitDTOs). */
    public void setUnits(List<UnitDTO> units) { this.units = units; }

    /** @param availableSlots Die Liste der verfügbaren Zeitfenster (FreeTimeDTOs). */
    public void setAvailableSlots(List<FreeTimeDTO> availableSlots) { this.availableSlots = availableSlots; }
}
package de.pse.oys.dto.plan;

import java.util.List;

/**
 * DTO für die Planung von Aufgaben. Enthält Informationen über den Planungshorizont, den aktuellen Zeitslot, blockierte Tage,
 * Präferenzzeiten, feste Blöcke und die zu planenden Aufgaben.
 * Dieses DTO wird als Übergabestruktur zwischen Microservice und dem Planning Service, des Backends verwendet.
 */
public class PlanningRequestDTO {
    private final int horizon;
    private final int currentSlot;
    private final List<Integer> blockedDays;
    private final String preferenceTime;
    private final List<FixedBlockDTO> fixedBlocks;
    private final List<PlanningTaskDTO> tasks;

    /**
     *
     * @param horizon Der Planungshorizont, der angibt, wie viele Zeiteinheiten in die Zukunft geplant werden soll.
     * @param currentSlot Der aktuelle Zeitslot, der angibt, ab welchem Slot die Planung beginnen soll.
     * @param blockedDays  Die Liste der blockierten Tage, an denen keine Aufgaben geplant werden sollen. Jeder Eintrag repräsentiert einen Tag (1 = Montag, 2 = Dienstag, ...).
     * @param preferenceTime Die Präferenzzeit, die angibt, zu welchen Tageszeiten die Planung bevorzugt stattfinden soll (z.B. "MORNING", "AFTERNOON", "EVENING").
     * @param fixedBlocks Die Liste der festen Blöcke, die bereits vorgegeben sind und in die Planung integriert werden müssen.
     * @param tasks Die Liste der zu planenden Aufgaben, die notwendige Informationen wie Umfang und Priorität festlegen.
     */
    public PlanningRequestDTO(int horizon, int currentSlot, List<Integer> blockedDays, String preferenceTime,
                              List<FixedBlockDTO> fixedBlocks, List<PlanningTaskDTO> tasks) {
        this.horizon = horizon;
        this.currentSlot = currentSlot;
        this.blockedDays = blockedDays;
        this.preferenceTime = preferenceTime;
        this.fixedBlocks = fixedBlocks;
        this.tasks = tasks;
    }

    /**
     * Getter für den Planungshorizont, der angibt, wie viele Zeiteinheiten in die Zukunft geplant werden soll.
     * @return der Planungshorizont in Slots.
     */
    public int getHorizon() {
        return horizon;
    }

    /**
     * Getter für den aktuellen Zeitslot, der angibt, ab welchem Slot die Planung beginnen soll.
     * WICHTIG: Es wird kein Usage angezeigt, da dies nur im Microservice relevant ist und daher von der IDE möglicherweise als ungenutzt markiert wird.
     * @return der aktuelle Zeitslot.
     */
    public int getCurrentSlot() {
        return currentSlot;
    }

    /**
     * Getter für die Liste der blockierten Tage,
     * an denen keine Aufgaben geplant werden sollen.
     * Jeder Eintrag repräsentiert einen Tag (1 = Montag, 2 = Dienstag, ...).
     * @return
     */
    public List<Integer> getBlockedDays() {
        return blockedDays;
    }

    /**
     * Getter für die Präferenzzeit,
     * die angibt, zu welchen Tageszeiten die Planung bevorzugt stattfinden soll
     * @see de.pse.oys.domain.enums.TimeSlot
     * @return die Präferenzzeit als String ("MORNING", "AFTERNOON", "EVENING"...).
     */
    public String getPreferenceTime() {
        return preferenceTime;
    }

    /**
     * Getter für die Liste der festen Blöcke, die bereits vorgegeben sind und in die Planung integriert werden müssen.
     * @return die Liste der festen Blöcke als {@link FixedBlockDTO}.
     */
    public List<FixedBlockDTO> getFixedBlocks() {
        return fixedBlocks;
    }

    /**
     * Getter für die Liste der zu planenden Aufgaben,
     * die notwendige Informationen wie Umfang und Priorität festlegen.
     * @return
     */
    public List<PlanningTaskDTO> getTasks() {
        return tasks;
    }



}

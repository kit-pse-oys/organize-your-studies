package de.pse.oys.dto;


import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.PerceivedDuration;
import de.pse.oys.domain.enums.ConcentrationLevel;

/**
 * RatingDTO – Datentransferobjekt für die Bewertung einer Lerneinheit.
 * Beinhaltet Bewertungen zu Ziel-Vollendung, wahrgenommener Dauer und Konzentration.
 *
 * @author uhupo
 * @version 1.0
 */
public class RatingDTO {

    private AchievementLevel goalCompletion;
    private PerceivedDuration perceivedDuration;
    private ConcentrationLevel concentration;

    /**
     * Konstruktor für das RatingDTO.
     *
     * @param goalCompletion    Bewertung der Ziel-Vollendung
     * @param perceivedDuration Bewertung der wahrgenommenen Dauer
     * @param concentration     Bewertung der Konzentration
     */
    public RatingDTO(AchievementLevel goalCompletion,
                     PerceivedDuration perceivedDuration,
                     ConcentrationLevel concentration) {
        this.goalCompletion = goalCompletion;
        this.perceivedDuration = perceivedDuration;
        this.concentration = concentration;
    }


    // Getter

    /**
     * Getter für die Ziel-Vollendung.
     * @return das goalCompletion Enum
     */
    public AchievementLevel getGoalCompletion() {
        return goalCompletion;
    }

    /**
     * Getter für die wahrgenommene Dauer.
     * @return das duration Enum
     */
    public PerceivedDuration getPerceivedDuration() {
        return perceivedDuration;
    }

    /**
     * Getter für die Konzentration.
     * @return das concentration Enum
     */
    public ConcentrationLevel getConcentration() {
        return concentration;
    }
}

package de.pse.oys.domain;

import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.domain.enums.PerceivedDuration;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * Repräsentiert das subjektive Feedback eines Nutzers zu einer abgeschlossenen Lerneinheit.
 * Die Bewertung umfasst die Konzentration, die Zeitwahrnehmung und den Lernerfolg.
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "ratings")
public class UnitRating {

    /** Eindeutige Kennung der Bewertung (ratingid). */
    @Id
    @Column(name = "ratingid", updatable = false)
    private UUID ratingId;

    /** Die subjektive Konzentrationsfähigkeit während der Einheit. */
    @Enumerated(EnumType.STRING)
    @Column(name = "concentration")
    private ConcentrationLevel concentration;

    /** Die subjektive Wahrnehmung der Dauer. */
    @Enumerated(EnumType.STRING)
    @Column(name = "duration_perception")
    private PerceivedDuration durationPerception;

    /** Der Grad der inhaltlichen Zielerreichung. */
    @Enumerated(EnumType.STRING)
    @Column(name = "achievement")
    private AchievementLevel achievement;

    /**
     * Standardkonstruktor für JPA/Hibernate.
     */
    protected UnitRating() {
    }

    /**
     * Erzeugt eine neue Bewertung für eine Lerneinheit.
     *
     * @param id            Eindeutige ID der Bewertung.
     * @param concentration Stufe der Konzentration.
     * @param durPercept    Wahrnehmung der Dauer.
     * @param achievement   Grad der Zielerreichung.
     */
    public UnitRating(UUID id, ConcentrationLevel concentration, PerceivedDuration durPercept, AchievementLevel achievement) {
        this.ratingId = id;
        this.concentration = concentration;
        this.durationPerception = durPercept;
        this.achievement = achievement;
    }

    // Getter

    /** @return Die ID der Bewertung. */
    public UUID getRatingId() {
        return ratingId;
    }

    /** @return Die gewählte Konzentrationsstufe. */
    public ConcentrationLevel getConcentration() {
        return concentration;
    }

    /** @return Die Wahrnehmung der Dauer. */
    public PerceivedDuration getPerceivedDuration() {
        return durationPerception;
    }

    /** @return Den Grad der Zielerreichung. */
    public AchievementLevel getAchievement() {
        return achievement;
    }
}
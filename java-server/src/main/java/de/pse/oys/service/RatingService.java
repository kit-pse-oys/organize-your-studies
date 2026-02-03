package de.pse.oys.service;

import de.pse.oys.domain.CostMatrix;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.UnitRating;
import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.domain.enums.PerceivedDuration;
import de.pse.oys.dto.controller.WrapperDTO;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.pse.oys.dto.RatingDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.persistence.RatingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * RatingService – Service für die Verwaltung von Bewertungen von Lerneinheiten
 * und das nachträgliche als verpasst Markieren.
 *
 * @author uhupo
 * @version 1.0
 */
@Service
public class RatingService {
    private static final String ERR_UNIT_NOT_FOUND = "Es wurde keine Lerneinheit mit der ID %s gefunden.";
    private static final String ERR_MATRIX_NOT_FOUND = "Keine CostMatrix für TaskId %s gefunden.";
    private final RatingRepository ratingRepository;
    private final LearningUnitRepository learningUnitRepository;
    private final CostMatrixRepository costMatrixRepository;

    /**
     * Konstruktor mit Dependency Injection.
     * @param ratingRepository das RatingRepository für das Speichern von Bewertungen.
     * @param learningUnitRepository das LearningUnitRepository für den Zugriff auf Lerneinheiten.
     * @param costMatrixRepository das CostMatrixRepository für den Zugriff auf Kostenmatrizen.
     */
    public RatingService(RatingRepository ratingRepository,
                         LearningUnitRepository learningUnitRepository,
                         CostMatrixRepository costMatrixRepository) {
        this.ratingRepository = ratingRepository;
        this.learningUnitRepository = learningUnitRepository;
        this.costMatrixRepository = costMatrixRepository;
    }

    /**
     * Speichert die Bewertung einer Lerneinheit.
     * Setzt die Bewertung in der Lerneinheit und speichert sie in der Datenbank.
     * Zudem wird die die Lerneinheit als abgeschlossen markiert.
     *
     * @param learningUnitId Die ID der Lerneinheit.
     * @param ratingDTO das RatingDTO mit den Bewertungen, wird in ein UnitRating umgewandelt.
     */
    @Transactional
    public void submitRating(UUID learningUnitId, RatingDTO ratingDTO) throws IllegalArgumentException {

        // Falls die Lerneinheit nicht gefunden wird, wird eine Exception geworfen.
        // Das bedeutet, dass die ID ungültig ist.
        LearningUnit learningUnit = learningUnitRepository.findById(learningUnitId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERR_UNIT_NOT_FOUND, learningUnitId)));

        AchievementLevel goalCompletion = ratingDTO.getGoalCompletion();
        PerceivedDuration perceivedDuration = ratingDTO.getPerceivedDuration();
        ConcentrationLevel concentration = ratingDTO.getConcentration();

        UnitRating unitRating = new UnitRating(concentration, perceivedDuration, goalCompletion);
        learningUnit.setRating(unitRating);

        // Da eine nächste Lernplanberechnung folgen kann und sich neue ungetrackte Bewertungen ergeben haben,
        // wird die zugehörige Kostenmatrix als veraltet markiert.
        Task task = learningUnit.getTask();
        CostMatrix costMatrix = costMatrixRepository.findByTask_TaskId(task.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(ERR_MATRIX_NOT_FOUND, task.getTaskId())));
        costMatrix.markAsOutdated();
        costMatrixRepository.save(costMatrix);

        // Unitrating wird durch CASCADE in LearningUnit mitgespeichert.
        learningUnitRepository.save(learningUnit);
    }

    /**
     * Markiert eine Lerneinheit nachträglich als verpasst.
     *<p>
     * Hinweis: Die Lerneinheit bleibt weiterhin im zugehörigen LearningPlan erhalten
     * und wird nur im Status auf {@link de.pse.oys.domain.enums.UnitStatus#MISSED} gesetzt.
     * Das bedeutet, dass die Einheit in der DB und im Plan referenziert bleibt,
     * aber als verpasst markiert ist.
     * Da das "verpasst markieren" nur rückwirkend erfolgt, werden keine neu verlegbaren Zeitslots blockiert.
     *</p>
     * @param unitId Die ID der Lerneinheit, die als verpasst markiert werden soll.
     */
    @Transactional
    public void markAsMissed(UUID unitId) throws IllegalArgumentException {

        // Falls die Lerneinheit nicht gefunden wird, wird eine Exception geworfen.
        // Das bedeutet, dass die ID ungültig ist.
        LearningUnit unit = learningUnitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERR_UNIT_NOT_FOUND, unitId)));
        unit.markAsMissed();

        learningUnitRepository.save(unit);
    }

    public List<WrapperDTO<RatingDTO>> getRatingsByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        // requireUserExists(userId); TODO
        return learningUnitRepository.findAllByTask_Module_User_UserId(userId).stream()
                .filter(unit -> unit.getRating() != null)
                .map(unit -> new WrapperDTO<>(unit.getUnitId(), toRatingDto(unit.getRating())))
                .toList();
    }

    // ---------------------------
    // private helper
    // ---------------------------

    private RatingDTO toRatingDto(UnitRating rating) {
        return new RatingDTO(
                rating.getAchievement(),
                rating.getPerceivedDuration(),
                rating.getConcentration()
        );
    }



}

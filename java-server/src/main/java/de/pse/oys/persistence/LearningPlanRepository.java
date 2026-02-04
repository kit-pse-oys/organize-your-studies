package de.pse.oys.persistence;

import de.pse.oys.domain.LearningPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für {@link LearningPlan}-Entitäten.
 *
 * @author uqvfm
 * @version 1.1
 */
@Repository
public interface LearningPlanRepository extends JpaRepository<LearningPlan, UUID> {

    /**
     * Lädt einen Lernplan nur dann, wenn er dem angegebenen User gehört.
     *
     * @param planId ID des Lernplans
     * @param userId ID des Users
     * @return Optional mit der gefundenen LearningPlan oder leer, wenn keine gefunden wurde.
     */
    Optional<LearningPlan> findByPlanIdAndUserId(UUID planId, UUID userId);

    /**
     * Findet den Lernplan eines Users für eine konkrete Startwoche.
     *
     * @param userId    ID des Users
     * @param weekStart Startdatum der Woche (Montag der Planwoche)
     * @return Optional mit dem gefundenen LearningPlan oder leer, wenn keine gefunden wurde.
     */
    Optional<LearningPlan> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}

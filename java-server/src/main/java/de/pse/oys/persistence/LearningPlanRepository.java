package de.pse.oys.persistence;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.LearningPlan;

/**
 * LearningPlanRepository – Repository-Schnittstelle für LearningPlan-Entitäten.
 */
@Repository
public interface LearningPlanRepository extends JpaRepository<LearningPlan, UUID> {

    /**
     * Findet den neuesten LearningPlan für einen bestimmten Benutzer mit einem bestimmten Status.
     * @param userId die ID des Benutzers
     * @param status der Status des Lernplans
     * @return Optional des neuesten LearningPlans mit dem angegebenen Status
     */
    @Query(value = """
            SELECT * FROM learning_plans
            WHERE userid = :uid AND status = cast(:status as plan_status)
            ORDER BY validity_week_start DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<LearningPlan> findByUserIdAndStatus(@Param("uid") UUID userId,
                                                 @Param("status") String status);

    /**
     * Findet einen LearningPlan für einen bestimmten Benutzer und eine bestimmte Startwoche.
     * @param userId die ID des Benutzers
     * @param weekStart das Startdatum der Woche
     * @return Optional des LearningPlans für den angegebenen Benutzer und die Woche
     */
    @Query(value = """
            SELECT * FROM learning_plans
            WHERE userid = :uid AND week_start = :weekStart
            LIMIT 1
            """, nativeQuery = true)
    Optional<LearningPlan> findByUserIdAndWeekStart(@Param("uid") UUID userId, @Param("weekStart") LocalDate weekStart);
}

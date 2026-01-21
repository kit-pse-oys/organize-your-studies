package de.pse.oys.persistence.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.plan.LearningPlan;

@Repository
public interface LearningPlanRepository extends JpaRepository<LearningPlan, UUID> {

    @Query(value = """
            select * from learning_plans
            where userid = :uid and status = cast(:status as plan_status)
            order by validity_week_start desc
            limit 1
            """, nativeQuery = true)
    Optional<LearningPlan> findByUserIdAndStatus(@Param("uid") UUID userId,
                                                 @Param("status") String status);
}

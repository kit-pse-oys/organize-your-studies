package de.pse.oys.persistence;

import de.pse.oys.domain.FreeTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * FreeTimeRepository – Repository-Schnittstelle für FreeTime-Entitäten.
 */
@Repository
public interface FreeTimeRepository extends JpaRepository<FreeTime, UUID> {

    List<FreeTime> findAllByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByUserIdAndFreeTimeIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID userId,
            UUID ignoreId,
            LocalTime endTime,
            LocalTime startTime
    );
}

package de.pse.oys.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.FreeTime;

/**
 * FreeTimeRepository – Repository-Schnittstelle für FreeTime-Entitäten.
 */
@Repository
public interface FreeTimeRepository extends JpaRepository<FreeTime, UUID> {

    /**
     * Findet alle FreeTime-Einträge für einen bestimmten Benutzer.
     * @param userId 
     * @return
     */
    @Query(value = "SELECT * FROM free_times WHERE userid = :uid", nativeQuery = true)
    List<FreeTime> findByUserId(@Param("uid") UUID userId);

    /**
     * Findet alle FreeTime-Einträge für einen bestimmten Benutzer in einem gegebenen Zeitraum.
     * @param userId die ID des Benutzers
     * @param start das Startdatum des Zeitraums
     * @param end das Enddatum des Zeitraums
     * @return Liste der FreeTime-Einträge im angegebenen Zeitraum
     */
    @Query(value = """
            SELECT * FROM free_times
            WHERE userid = :uid
              AND (
                    (specific_date IS NOT NULL AND specific_date BETWEEN :start AND :end)
                 OR (weekday IS NOT NULL)
              )
            """, nativeQuery = true)
    List<FreeTime> findFreeTimeInPeriod(@Param("uid") UUID userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);
}


package de.pse.oys.persistence;

import de.pse.oys.domain.FreeTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * FreeTimeRepository – Repository-Schnittstelle für FreeTime-Entitäten.
 */
@Repository
public interface FreeTimeRepository extends JpaRepository<FreeTime, UUID> {

    /**
     * Findet alle FreeTime-Einträge für einen bestimmten Benutzer.
     * @param userId die ID des Benutzers
     * @return Liste der FreeTime-Einträge des Benutzers
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


    /**
     * DB-seitiger Overlap-Check für einen konkreten Tag (dto.date) inkl. Weekly-Blocks.
     * Zeitüberlappung: [start, end)
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM free_times ft
                WHERE ft.userid = :uid
                  AND (:ignoreId IS NULL OR ft.slotid <> :ignoreId)
                  AND ft.start_time < :endTime
                  AND :startTime < ft.end_time
                  AND (
                        (ft.specific_date IS NOT NULL AND ft.specific_date = :date)
                     OR (ft.weekday IS NOT NULL AND ft.weekday = CAST(:weekday AS recurring_day))
                  )
            )
            """, nativeQuery = true)
    boolean existsOverlap(@Param("uid") UUID userId,
                          @Param("date") LocalDate date,
                          @Param("weekday") String weekday,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime,
                          @Param("ignoreId") UUID ignoreId);
}

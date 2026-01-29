package de.pse.oys.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.Task;

/**
 * Repository für {@link Task}-Entitäten.
 * <p>
 * Die Abfragen werden zusätzlich über {@code userId} auf einen Nutzer eingeschränkt
 * (Ownership: User → Module → Task), indem über {@code modules.userid} gefiltert wird.
 * </p>
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Findet alle Tasks mit einer Deadline vor dem angegebenen Datum, eingeschränkt auf einen User.
     * <p>
     * Berücksichtigt {@code fixed_deadline} sowie {@code time_frame_end}.
     * </p>
     *
     * @param userId   ID des Users, auf dessen Daten eingeschränkt wird
     * @param deadline Stichtag; Deadlines müssen strikt davor liegen
     * @return Liste der passenden Tasks innerhalb des User-Scopes
     */
    @Query(value = """
        SELECT
            t.*,
            t.weekly_effort_minutes AS weekly_duration_minutes
        FROM tasks t
        JOIN modules m ON m.moduleid = t.moduleid
        WHERE m.userid = :userId
          AND (
                (t.fixed_deadline IS NOT NULL AND t.fixed_deadline < :deadline)
             OR (t.time_frame_end IS NOT NULL AND t.time_frame_end < :deadline)
          )
        """, nativeQuery = true)
    List<Task> findAllByDeadlineBefore(@Param("userId") UUID userId,
                                       @Param("deadline") LocalDate deadline);

    /**
     * Findet alle Tasks, die zu einem bestimmten Modul gehören, eingeschränkt auf einen User.
     *
     * @param userId   ID des Users, auf dessen Daten eingeschränkt wird
     * @param moduleId ID des Moduls
     * @return Liste der Tasks des Moduls innerhalb des User-Scopes
     */
    @Query(value = """
        SELECT
            t.*,
            t.weekly_effort_minutes AS weekly_duration_minutes
        FROM tasks t
        JOIN modules m ON m.moduleid = t.moduleid
        WHERE t.moduleid = :moduleId
          AND m.userid = :userId
        """, nativeQuery = true)
    List<Task> findByModuleId(@Param("userId") UUID userId,
                              @Param("moduleId") UUID moduleId);

    /**
     * Findet alle Tasks eines Users mit einem bestimmten Status.
     * <p>
     * Erwartete Werte für {@code status} entsprechen dem Datenbank-Enum {@code task_status},
     * z. B. {@code "active"} oder {@code "completed"}.
     * </p>
     *
     * @param userId  ID des Users, auf dessen Daten eingeschränkt wird
     * @param status  Statuswert gemäß {@code task_status} (z. B. {@code "active"}, {@code "completed"})
     * @return Liste der Tasks des Users mit dem angegebenen Status
     */
    @Query(value = """
        SELECT
            t.*,
            t.weekly_effort_minutes AS weekly_duration_minutes
        FROM tasks t
        JOIN modules m ON m.moduleid = t.moduleid
        WHERE m.userid = :userId
          AND t.status = :status
        """, nativeQuery = true)
    List<Task> findAllByUserAndStatus(@Param("userId") UUID userId,
                                      @Param("status") String status);
}




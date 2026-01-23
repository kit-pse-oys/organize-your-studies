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
 * TaskRepository – Repository-Schnittstelle für Task-Entitäten und dessen Sub-Emtitäten.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Findet alle Tasks eines Nutzers, deren Deadline vor dem angegebenen Datum liegt.
     * Berücksichtigt sowohl fixe Deadlines (fixed_deadline) als auch das Ende eines Zeitfensters (time_frame_end).
     *
     * @param userId ID des Nutzers
     * @param deadline Stichtag (exklusiv)
     * @return Liste der Tasks des Nutzers mit Deadline vor {@code deadline}
     */
    @Query(value = """
        SELECT t.*
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
     * Findet alle Tasks, die zu einem bestimmten Modul gehören.
     * @param moduleId die ID des Moduls
     * @return Liste der Tasks, die zu dem angegebenen Modul gehören
     */
    @Query(value = "SELECT * FROM tasks WHERE moduleid = :mid", nativeQuery = true)
    List<Task> findByModuleId(@Param("mid") UUID moduleId);

    /**
     * Findet alle aktiven (nicht abgeschlossenen) Tasks eines Nutzers.
     *
     * <p>Hinweis zum Schema: Ein Task gehört zu einem Modul (tasks.moduleid),
     * und ein Modul gehört zu einem Nutzer (modules.userid). Daher wird über
     * die Module-Tabelle gejoint, um die Tasks eines bestimmten Nutzers zu finden.</p>
     *
     * @param userId ID des Nutzers
     * @return Liste aller aktiven Tasks des Nutzers
     */
    @Query(value = """
        SELECT t.*
        FROM tasks t
        JOIN modules m ON m.moduleid = t.moduleid
        WHERE m.userid = :userId
          AND t.status = 'active'
        """, nativeQuery = true)
    List<Task> findActiveTasksByUserId(@Param("userId") UUID userId);


    // TODO: @Marcel - Das ist nur ein Provisorisorische lösung bitte später auf sauberes JPA umstelen oder so ka du bist experte zB findAllByUserAndStatus  .
    @Query(value = "SELECT * FROM tasks WHERE user_id = :userId AND status = 'OPEN'", nativeQuery = true)
    List<Task> findOpenTasksByUserId(@Param("userId") UUID userId);
}



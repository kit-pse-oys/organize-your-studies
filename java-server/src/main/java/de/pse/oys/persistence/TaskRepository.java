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
     * Findet alle Tasks mit einem Deadline-Datum vor dem angegebenen Datum.
     * @param deadline das Datum, vor dem die Deadlines liegen sollen
     * @return Liste der Tasks mit Deadlines vor dem angegebenen Datum
     * //FIXME: UserId fehlt noch als parameter, evtl bei weiteren repos auch
     */
    @Query(value = """
            SELECT * FROM tasks
            WHERE (fixed_deadline IS NOT NULL AND fixed_deadline < :d)
               OR (time_frame_end IS NOT NULL AND time_frame_end < :d)
            """, nativeQuery = true)
    List<Task> findAllByDeadlineBefore(@Param("d") LocalDate deadline);

    /**
     * Findet alle Tasks, die zu einem bestimmten Modul gehören.
     * @param moduleId die ID des Moduls
     * @return Liste der Tasks, die zu dem angegebenen Modul gehören
     */
    @Query(value = "SELECT * FROM tasks WHERE moduleid = :mid", nativeQuery = true)
    List<Task> findByModuleId(@Param("mid") UUID moduleId);
}

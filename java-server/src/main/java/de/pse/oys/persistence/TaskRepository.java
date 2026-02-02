package de.pse.oys.persistence;

import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.OtherTask;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für {@link Task}-Entitäten.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Findet alle Tasks eines Users (Ownership über Task -> Module -> User).
     *
     * @param userId ID des Users
     * @return alle Tasks dieses Users
     */
    @Query("SELECT t FROM Task t WHERE t.module.user.userId = :userId")
    List<Task> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Findet eine Task anhand (taskId, userId).
     *
     * @param taskId ID der Task
     * @param userId ID des Users
     * @return Task, falls existent und dem User zugehörig
     */
    @Query("SELECT t FROM Task t WHERE t.taskId = :taskId AND t.module.user.userId = :userId")
    Optional<Task> findByIdAndUserId(@Param("taskId") UUID taskId,
                                     @Param("userId") UUID userId);

    /**
     * Findet alle Tasks, die zu einem bestimmten Modul gehören (Ownership abgesichert).
     *
     * @param userId   ID des Users
     * @param moduleId ID des Moduls
     * @return Liste der Tasks des Moduls innerhalb des User-Scopes
     */
    @Query("SELECT t FROM Task t WHERE t.module.moduleId = :moduleId AND t.module.user.userId = :userId")
    List<Task> findByModuleId(@Param("userId") UUID userId,
                              @Param("moduleId") UUID moduleId);

    /**
     * Findet alle Tasks eines Users mit einem bestimmten Status.
     *
     * @param userId ID des Users
     * @param status Status (Enum)
     * @return Liste der Tasks des Users mit dem angegebenen Status
     */
    @Query("SELECT t FROM Task t WHERE t.module.user.userId = :userId AND t.status = :status")
    List<Task> findAllByUserAndStatus(@Param("userId") UUID userId,
                                      @Param("status") TaskStatus status);
}
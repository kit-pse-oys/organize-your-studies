package de.pse.oys.persistence;

import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für {@link Task}-Entitäten.
 *
 * @author uqvfm
 * @version 1.1
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Liefert alle Aufgaben eines Nutzers.
     * <p>
     * Die Zuordnung erfolgt indirekt über das zugehörige Modul:
     * {@code Task -> Module -> User}. Es werden alle Tasks zurückgegeben,
     * deren Modul dem Nutzer mit der angegebenen {@code userId} gehört.
     * </p>
     *
     * @param userId ID des Nutzers, dessen Aufgaben abgefragt werden.
     * @return Liste aller Aufgaben des Nutzers (leer, wenn keine vorhanden sind).
     */
    List<Task> findAllByModuleUserUserId(UUID userId);

    /**
     * Findet eine Task anhand (taskId, userId) im User-Scope.
     * <p>
     * Die Task wird nur zurückgegeben, wenn sie existiert und ihr Modul dem Nutzer gehört.
     * </p>
     *
     * @param taskId ID der Task.
     * @param userId ID des Users.
     * @return Task, falls existent und dem User zugehörig.
     */
    Optional<Task> findByTaskIdAndModuleUserUserId(UUID taskId, UUID userId);

    /**
     * Findet alle Tasks, die zu einem bestimmten Modul gehören (Ownership abgesichert).
     * <p>
     * Es werden nur Tasks zurückgegeben, deren Modul die angegebene {@code moduleId} hat
     * und gleichzeitig dem Nutzer mit {@code userId} gehört.
     * </p>
     *
     * @param userId   ID des Users.
     * @param moduleId ID des Moduls.
     * @return Liste der Tasks des Moduls innerhalb des User-Scopes.
     */
    List<Task> findAllByModuleModuleIdAndModuleUserUserId(UUID moduleId, UUID userId);

    /**
     * Findet alle Tasks eines Users mit einem bestimmten Status.
     *
     * @param userId ID des Users.
     * @param status Status (Enum).
     * @return Liste der Tasks des Users mit dem angegebenen Status.
     */
    List<Task> findAllByModuleUserUserIdAndStatus(UUID userId, TaskStatus status);
}

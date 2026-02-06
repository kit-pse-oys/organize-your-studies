package de.pse.oys.persistence;

import de.pse.oys.domain.Task;
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
     * Liefert alle Aufgaben eines Nutzers
     *
     * @param userId ID des Nutzers, dessen Aufgaben abgefragt werden
     * @return Liste aller Aufgaben des Nutzers
     */
    List<Task> findAllByModuleUserUserId(UUID userId);

    /**
     * Findet eine Task anhand (taskId, userId) im User-Scope.
     *
     * @param taskId ID der Task
     * @param userId ID des Users
     * @return Task, falls existent und dem User zugehörig
     */
    Optional<Task> findByTaskIdAndModuleUserUserId(UUID taskId, UUID userId);

    /**
     * Findet alle Tasks, die zu einem bestimmten Modul gehören.
     *
     * @param userId   ID des Users
     * @param moduleId ID des Moduls
     * @return Liste der Tasks des Moduls innerhalb des User-Scopes
     */
    List<Task> findAllByModuleModuleIdAndModuleUserUserId(UUID moduleId, UUID userId);
}

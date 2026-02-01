package de.pse.oys.service;

import de.pse.oys.dto.TaskDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Skeleton-Service für Tasks: nur damit es kompiliert.
 * Business-Logik wird später wieder ergänzt.
 */
@Service
public class TaskService {

    private static final String MSG_USER_ID_NULL = "userId darf nicht null sein.";
    private static final String MSG_USER_NOT_FOUND = "User nicht gefunden.";
    private static final String MSG_TASK_ID_NULL = "taskId darf nicht null sein.";

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final TaskRepository taskRepository;

    public TaskService(UserRepository userRepository,
                       ModuleRepository moduleRepository,
                       TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskDTO createTask(UUID userId, TaskDTO dto) {
        requireExistingUser(userId);
        // Skeleton: keine Logik, nur compilable
        throw new UnsupportedOperationException("TaskService#createTask ist noch nicht implementiert.");
    }

    @Transactional
    public TaskDTO updateTask(UUID userId, TaskDTO dto) {
        requireExistingUser(userId);
        // Skeleton: keine Logik, nur compilable
        throw new UnsupportedOperationException("TaskService#updateTask ist noch nicht implementiert.");
    }

    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        requireExistingUser(userId);
        if (taskId == null) {
            throw new IllegalArgumentException(MSG_TASK_ID_NULL);
        }
        // Skeleton: optional no-op oder später implementieren
        throw new UnsupportedOperationException("TaskService#deleteTask ist noch nicht implementiert.");
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(UUID userId) {
        requireExistingUser(userId);
        // Skeleton: leere Liste zurückgeben
        return Collections.emptyList();
    }

    // ===== helpers =====

    private void requireExistingUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(MSG_USER_ID_NULL);
        }
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(MSG_USER_NOT_FOUND);
        }
    }
}

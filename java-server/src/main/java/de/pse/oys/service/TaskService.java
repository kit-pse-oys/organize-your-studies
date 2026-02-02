package de.pse.oys.service;

import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.OtherTask;
import de.pse.oys.domain.SubmissionTask;
import de.pse.oys.domain.Task;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * TaskService kapselt die Geschäftslogik für Aufgaben:
 * - Laden aller Tasks eines Users
 * - Erstellen, Updaten und Löschen von Tasks
 * - Validierung von DTOs und Mapping DTO <-> Entity
 *
 * Hinweis: sendNotification ist aktuell NICHT als Feld in der Task-Entity persistiert.
 * Deshalb wird es im Mapping bewusst konstant gesetzt (siehe fillBase).
 *
 * @author uqvfm
 * @version 1.1
 */
@Service
@Transactional
public class TaskService {

    private static final int MINUTES_PER_WEEK = 7 * 24 * 60;

    private static final String MSG_REQUIRED_FIELDS_MISSING = "Pflichtfelder fehlen.";
    private static final String MSG_WEEKLY_LOAD_INVALID = "weeklyTimeLoad muss > 0 und <= Minuten einer Woche sein.";
    private static final String MSG_USER_NOT_FOUND = "User existiert nicht.";
    private static final String MSG_MODULE_NOT_FOUND = "Modul existiert nicht oder gehört nicht zum User.";
    private static final String MSG_TASK_NOT_FOUND = "Task existiert nicht.";
    private static final String MSG_TASK_NOT_OWNED = "Task gehört nicht zum User.";
    private static final String MSG_CATEGORY_CHANGE_FORBIDDEN = "TaskCategory darf beim Update nicht geändert werden.";
    private static final String MSG_SUBMISSION_CYCLE_INVALID = "submissionCycle muss >= 1 sein.";
    private static final String MSG_SUBMISSION_RANGE_INVALID = "endTime muss nach firstDeadline liegen.";
    private static final String MSG_OTHER_RANGE_INVALID = "Bei sonstigen Aufgaben muss endTime größer als startTime sein.";
    private static final String MSG_DTO_TYPE_MISMATCH = "DTO-Typ oder Task-Subtyp passt nicht zur Kategorie.";

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

    /**
     * Liefert alle Tasks eines Users als DTOs.
     *
     * @param userId ID des Nutzers (darf nicht {@code null} sein)
     * @return Liste aller Tasks des Nutzers als {@link TaskDTO}
     * @throws NullPointerException      wenn {@code userId} {@code null} ist
     * @throws ResourceNotFoundException wenn der Nutzer nicht existiert
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        requireUserExists(userId);

        return taskRepository.findAllByUserId(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Erstellt eine neue Task für einen Nutzer.
     *
     * <p>Ablauf:
     * DTO validieren, User prüfen, Modul des Users laden, Entity erzeugen und dem Modul zuordnen
     * (bidirektional), speichern und als DTO zurückgeben.</p>
     *
     * @param userId ID des Nutzers (darf nicht {@code null} sein)
     * @param dto    Task-Daten als DTO (darf nicht {@code null} sein)
     * @return die gespeicherte Task als DTO
     * @throws NullPointerException      wenn {@code userId} {@code null} ist
     * @throws ValidationException       wenn das DTO ungültig ist
     * @throws ResourceNotFoundException wenn Nutzer oder Modul nicht existiert/gehört
     */
    public TaskDTO createTask(UUID userId, TaskDTO dto) {
        Objects.requireNonNull(userId, "userId");
        validateData(dto);
        requireUserExists(userId);
        Module module = requireOwnedModule(userId, dto.getModuleTitle());
        Task task = mapToEntity(dto);
        module.addTask(task);
        return mapToDto(taskRepository.save(task));
    }

    /**
     * Aktualisiert eine bestehende Task.
     *
     * <p>Wichtig: Die Kategorie darf nicht geändert werden, da das einem Typwechsel
     * (z.B. ExamTask -> OtherTask) entsprechen würde.</p>
     *
     * @param userId ID des Nutzers (darf nicht {@code null} sein)
     * @param taskId ID der zu aktualisierenden Task (darf nicht {@code null} sein)
     * @param dto    neue Task-Daten als DTO (darf nicht {@code null} sein)
     * @return die aktualisierte Task als DTO
     * @throws NullPointerException      wenn {@code userId} oder {@code taskId} {@code null} ist
     * @throws ValidationException       wenn das DTO ungültig ist oder die Kategorie geändert würde
     * @throws ResourceNotFoundException wenn Nutzer, Modul oder Task nicht existiert
     * @throws AccessDeniedException     wenn die Task existiert, aber nicht dem Nutzer gehört
     */
    public TaskDTO updateTask(UUID userId, UUID taskId, TaskDTO dto) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(taskId, "taskId");
        validateData(dto);
        requireUserExists(userId);

        Task task = requireOwnedTask(userId, taskId);

        // Kategorie-Wechsel würde Typwechsel bedeuten -> nicht erlaubt
        if (dto.getCategory() != task.getCategory()) {
            throw new ValidationException(MSG_CATEGORY_CHANGE_FORBIDDEN);
        }

        Module newModule = requireOwnedModule(userId, dto.getModuleTitle());
        Module oldModule = task.getModule();

        // Falls Modulwechsel: Collection im alten Modul aktualisieren und neues Modul setzen
        if (oldModule != null && oldModule != newModule) {
            oldModule.deleteTask(task);
            newModule.addTask(task);
        } else {
            task.setModule(newModule);
        }

        task.setTitle(dto.getTitle());
        task.setWeeklyDurationMinutes(dto.getWeeklyTimeLoad());
        applySubtypeFields(task, dto);

        return mapToDto(taskRepository.save(task));
    }

    /**
     * Löscht eine bestehende Task.
     *
     * <p>Die Task wird zunächst aus der Task-Liste ihres Moduls entfernt, damit die
     * bidirektionale Beziehung im aktuellen Persistence-Context konsistent bleibt.
     * Anschließend wird die Task über das Repository aus der Datenbank gelöscht.</p>
     *
     * @param userId ID des Nutzers (darf nicht {@code null} sein)
     * @param taskId ID der zu löschenden Task (darf nicht {@code null} sein)
     * @throws NullPointerException      wenn {@code userId} oder {@code taskId} {@code null} ist
     * @throws ResourceNotFoundException wenn Nutzer oder Task nicht existiert
     * @throws AccessDeniedException     wenn die Task existiert, aber nicht dem Nutzer gehört
     */
    public void deleteTask(UUID userId, UUID taskId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(taskId, "taskId");
        requireUserExists(userId);

        Task task = requireOwnedTask(userId, taskId);

        Module module = task.getModule();
        if (module != null) {
            module.deleteTask(task);
        }

        taskRepository.delete(task);
    }

    /**
     * Validiert das DTO auf Pflichtfelder + fachliche Regeln pro Kategorie.
     */
    private void validateData(TaskDTO dto) {
        if (dto == null
                || isBlank(dto.getTitle())
                || isBlank(dto.getModuleTitle())
                || dto.getCategory() == null
                || dto.getWeeklyTimeLoad() == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        int weekly = dto.getWeeklyTimeLoad();
        if (weekly <= 0 || weekly > MINUTES_PER_WEEK) {
            throw new ValidationException(MSG_WEEKLY_LOAD_INVALID);
        }

        switch (dto.getCategory()) {
            case EXAM -> {
                ExamTaskDTO exam = (ExamTaskDTO) dto;
                if (exam.getExamDate() == null) {
                    throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
                }
            }
            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;

                if (sub.getFirstDeadline() == null || sub.getEndTime() == null || sub.getSubmissionCycle() == null) {
                    throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
                }
                if (sub.getSubmissionCycle() < 1) {
                    throw new ValidationException(MSG_SUBMISSION_CYCLE_INVALID);
                }

                LocalDateTime first = sub.getFirstDeadline();
                LocalDateTime end = sub.getEndTime();
                if (!end.isAfter(first)) {
                    throw new ValidationException(MSG_SUBMISSION_RANGE_INVALID);
                }
            }
            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;

                if (other.getStartTime() == null || other.getEndTime() == null) {
                    throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
                }
                if (!other.getEndTime().isAfter(other.getStartTime())) {
                    throw new ValidationException(MSG_OTHER_RANGE_INVALID);
                }
            }
        }
    }

    /**
     * Erstellt aus einem DTO die passende Task-Entity (je nach Kategorie/Unterklasse).
     */
    private Task mapToEntity(TaskDTO dto) {
        String title = dto.getTitle();
        int weekly = dto.getWeeklyTimeLoad();

        return switch (dto.getCategory()) {
            case EXAM -> {
                ExamTaskDTO exam = (ExamTaskDTO) dto;
                yield new ExamTask(title, weekly, exam.getExamDate());
            }
            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;
                yield new SubmissionTask(
                        title,
                        weekly,
                        sub.getFirstDeadline(),
                        sub.getSubmissionCycle(),
                        sub.getEndTime()
                );
            }
            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;
                yield new OtherTask(title, weekly, other.getStartTime(), other.getEndTime());
            }
        };
    }

    // ---------------------------
    // private helper
    // ---------------------------

    /**
     * Prüft, ob der User existiert.
     */
    private void requireUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(MSG_USER_NOT_FOUND);
        }
    }

    /**
     * Lädt ein Modul anhand (userId, moduleTitle) oder wirft eine Exception,
     * wenn es nicht existiert/ dem User nicht gehört.
     */
    private Module requireOwnedModule(UUID userId, String moduleTitle) {
        return moduleRepository.findByUserIdAndTitle(userId, moduleTitle)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_MODULE_NOT_FOUND));
    }

    /**
     * Lädt eine Task anhand (taskId, userId).
     * Wenn taskId existiert, aber nicht dem User gehört -> AccessDenied.
     * Wenn taskId nicht existiert -> NotFound.
     */
    private Task requireOwnedTask(UUID userId, UUID taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> taskRepository.existsById(taskId)
                        ? new AccessDeniedException(MSG_TASK_NOT_OWNED)
                        : new ResourceNotFoundException(MSG_TASK_NOT_FOUND));
    }

    /**
     * Setzt nur die spezifischen Felder des jeweiligen Subtyps (Exam/Submission/Other).
     */
    private void applySubtypeFields(Task task, TaskDTO dto) {
        switch (task.getCategory()) {
            case EXAM -> ((ExamTask) task).setExamDate(((ExamTaskDTO) dto).getExamDate());
            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;
                SubmissionTask st = (SubmissionTask) task;
                st.setFirstDeadline(sub.getFirstDeadline());
                st.setCycleWeeks(sub.getSubmissionCycle());
                st.setEndTime(sub.getEndTime());
            }
            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;
                OtherTask ot = (OtherTask) task;
                ot.setStartTime(other.getStartTime());
                ot.setEndTime(other.getEndTime());
            }
        }
    }

    /**
     * Mappt eine Task-Entity in das passende DTO.
     */
    private TaskDTO mapToDto(Task task) {
        return switch (task.getCategory()) {
            case EXAM -> {
                ExamTaskDTO dto = new ExamTaskDTO();
                fillBase(dto, task);
                dto.setExamDate(((ExamTask) task).getExamDate());
                yield dto;
            }
            case SUBMISSION -> {
                SubmissionTask st = (SubmissionTask) task;
                SubmissionTaskDTO dto = new SubmissionTaskDTO();
                fillBase(dto, task);
                dto.setFirstDeadline(st.getFirstDeadline());
                dto.setSubmissionCycle(st.getCycleWeeks());
                dto.setEndTime(st.getEndTime());
                yield dto;
            }
            case OTHER -> {
                OtherTask ot = (OtherTask) task;
                OtherTaskDTO dto = new OtherTaskDTO();
                fillBase(dto, task);
                dto.setStartTime(ot.getStartTime());
                dto.setEndTime(ot.getEndTime());
                yield dto;
            }
        };
    }

    /**
     * Setzt die Felder, die in allen TaskDTOs gleich sind.
     */
    private static void fillBase(TaskDTO dto, Task task) {
        dto.setTitle(task.getTitle());
        dto.setModuleTitle(task.getModule() != null ? task.getModule().getTitle() : null);
        dto.setCategory(task.getCategory());
        dto.setWeeklyTimeLoad(task.getWeeklyDurationMinutes());

        // aktuell nicht persistierbar -> bewusst konstant
        dto.setSendNotification(false); // TODO: falls später persistiert, hier mappen
    }

    /**
     * Kleiner Helper für String-Pflichtfelder.
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

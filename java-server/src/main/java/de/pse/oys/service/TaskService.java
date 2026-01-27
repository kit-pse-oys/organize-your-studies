package de.pse.oys.service;

import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.OtherTask;
import de.pse.oys.domain.SubmissionTask;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.Weekday;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service für Tasks: erstellen, ändern, löschen und abrufen.
 *
 * @author uqvfm
 * @version 1.0
 */
@Service
public class TaskService {

    private static final int DAYS_PER_WEEK = 7;
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    private static final int LAST_HOUR_OF_DAY = 23;
    private static final int LAST_MINUTE_OF_DAY = 59;

    private static final int NORMALIZED_SECOND = 0;
    private static final int NORMALIZED_NANO = 0;

    private static final int MAX_WEEKLY_MINUTES = DAYS_PER_WEEK * HOURS_PER_DAY * MINUTES_PER_HOUR;
    private static final LocalTime LATEST_TIME = LocalTime.of(LAST_HOUR_OF_DAY, LAST_MINUTE_OF_DAY);

    private static final boolean DEFAULT_SEND_NOTIFICATION = false;
    private static final int DEFAULT_SUBMISSION_CYCLE = 1;

    private static final String MSG_TASK_DTO_NULL = "TaskDTO darf nicht null sein.";
    private static final String MSG_TITLE_BLANK = "title darf nicht leer sein.";
    private static final String MSG_MODULE_TITLE_BLANK = "moduleTitle darf nicht leer sein.";
    private static final String MSG_CATEGORY_NULL = "category darf nicht null sein.";

    private static final String MSG_WEEKLY_NULL_OR_LEQ_ZERO = "weeklyTimeLoad muss > 0 sein.";
    private static final String MSG_WEEKLY_TOO_LARGE_TEMPLATE = "weeklyTimeLoad darf nicht größer als %d Minuten sein.";

    private static final String MSG_EXAM_REQUIRES_EXAM_DTO = "EXAM erfordert ExamTaskDTO.";
    private static final String MSG_EXAM_DATE_NULL = "examDate darf nicht null sein.";

    private static final String MSG_SUBMISSION_REQUIRES_SUBMISSION_DTO = "SUBMISSION erfordert SubmissionTaskDTO.";
    private static final String MSG_SUBMISSION_DAY_NULL = "submissionDay darf nicht null sein.";
    private static final String MSG_SUBMISSION_TIME_NULL = "submissionTime darf nicht null sein.";
    private static final String MSG_SUBMISSION_TIME_TOO_LATE = "submissionTime darf nicht nach 23:59 sein.";

    private static final String MSG_OTHER_REQUIRES_OTHER_DTO = "OTHER erfordert OtherTaskDTO.";
    private static final String MSG_OTHER_DATES_NULL = "startDate/endDate dürfen nicht null sein.";
    private static final String MSG_OTHER_END_BEFORE_START = "endDate muss >= startDate sein.";

    private static final String MSG_UNKNOWN_TASK_CATEGORY = "Unbekannte TaskCategory.";
    private static final String MSG_TYPE_CHANGE_NOT_ALLOWED = "Task-Typ kann nicht geändert werden.";

    private static final String MSG_USER_ID_NULL = "userId darf nicht null sein.";
    private static final String MSG_USER_NOT_FOUND = "User nicht gefunden.";

    private static final String MSG_TASK_ID_NULL = "taskId darf nicht null sein.";
    private static final String MSG_TASK_NOT_FOUND = "Task nicht gefunden.";
    private static final String MSG_ACCESS_DENIED = "Kein Zugriff auf diese Aufgabe.";

    private static final String MSG_MODULE_NOT_FOUND_TEMPLATE = "Modul nicht gefunden: %s";
    private static final String MSG_TASK_NOT_FOUND_GENERIC = "Task nicht gefunden.";
    private static final String MSG_TASK_DUPLICATE_TITLE = "Mehrere Tasks mit gleichem Titel im Modul gefunden.";

    private static final String MSG_UNKNOWN_TASK_TYPE_TEMPLATE = "Unbekannter Task-Typ: %s";

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final TaskRepository taskRepository;

    /**
     * Erstellt den Service.
     *
     * @param userRepository Repository für User
     * @param moduleRepository Repository für Module
     * @param taskRepository Repository für Tasks
     */
    public TaskService(UserRepository userRepository,
                       ModuleRepository moduleRepository,
                       TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Legt eine neue Task für den User an.
     *
     * @param userId User-ID
     * @param dto Task-Daten
     * @return gespeicherte Task als DTO
     */
    @Transactional
    public TaskDTO createTask(UUID userId, TaskDTO dto) {
        requireExistingUser(userId);
        validateData(dto);

        Module module = resolveModuleForUser(userId, dto.getModuleTitle());

        Task entity = mapToEntity(dto);
        entity.setModule(module);

        Task saved = taskRepository.save(entity);
        return mapToDto(saved);
    }

    /**
     * Aktualisiert eine bestehende Task.
     *
     * @param userId User-ID
     * @param dto neue Daten
     * @return aktualisierte Task als DTO
     */
    @Transactional
    public TaskDTO updateTask(UUID userId, TaskDTO dto) {
        requireExistingUser(userId);
        validateData(dto);

        Module module = resolveModuleForUser(userId, dto.getModuleTitle());
        Task existing = findTaskByNaturalKey(userId, module.getModuleId(), dto.getTitle(), dto.getCategory());

        existing.setTitle(dto.getTitle());
        existing.setWeeklyDurationMinutes(dto.getWeeklyTimeLoad());
        existing.setModule(module);

        switch (dto.getCategory()) {
            case EXAM -> {
                if (!(existing instanceof ExamTask exam)) {
                    throw new IllegalArgumentException(MSG_TYPE_CHANGE_NOT_ALLOWED);
                }
                ExamTaskDTO examDto = (ExamTaskDTO) dto;
                exam.setExamDate(examDto.getExamDate());
            }

            case SUBMISSION -> {
                if (!(existing instanceof SubmissionTask sub)) {
                    throw new IllegalArgumentException(MSG_TYPE_CHANGE_NOT_ALLOWED);
                }
                SubmissionTaskDTO subDto = (SubmissionTaskDTO) dto;
                LocalDateTime deadline = computeNextSubmissionDeadline(subDto.getSubmissionDay(), subDto.getSubmissionTime());
                sub.setDeadline(deadline);
            }

            case OTHER -> {
                if (!(existing instanceof OtherTask other)) {
                    throw new IllegalArgumentException(MSG_TYPE_CHANGE_NOT_ALLOWED);
                }
                OtherTaskDTO otherDto = (OtherTaskDTO) dto;
                other.setStartTime(otherDto.getStartDate().atStartOfDay());
                other.setEndTime(otherDto.getEndDate().atTime(LATEST_TIME));
            }

            default -> throw new IllegalArgumentException(MSG_UNKNOWN_TASK_CATEGORY);
        }

        Task saved = taskRepository.save(existing);
        return mapToDto(saved);
    }

    /**
     * Löscht eine Task, wenn sie dem User gehört.
     *
     * @param userId User-ID
     * @param taskId Task-ID
     */
    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        requireExistingUser(userId);

        if (taskId == null) {
            throw new IllegalArgumentException(MSG_TASK_ID_NULL);
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(MSG_TASK_NOT_FOUND));

        UUID moduleId = (task.getModule() != null) ? task.getModule().getModuleId() : null;
        if (moduleId == null || !userOwnsModule(userId, moduleId)) {
            throw new SecurityException(MSG_ACCESS_DENIED);
        }

        taskRepository.delete(task);
    }

    /**
     * Liefert alle Tasks des Users.
     *
     * @param userId User-ID
     * @return Tasks als DTO-Liste
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(UUID userId) {
        requireExistingUser(userId);

        List<Module> modules = moduleRepository.findByUserId(userId);
        List<TaskDTO> result = new ArrayList<>();

        for (Module module : modules) {
            List<Task> tasks = taskRepository.findByModuleId(userId, module.getModuleId());
            for (Task task : tasks) {
                result.add(mapToDto(task));
            }
        }

        result.sort(Comparator.comparing(TaskDTO::getModuleTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(TaskDTO::getTitle, String.CASE_INSENSITIVE_ORDER));

        return result;
    }

    /**
     * Prüft die DTO-Daten.
     *
     * @param dto TaskDTO
     */
    private void validateData(TaskDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(MSG_TASK_DTO_NULL);
        }

        if (isBlank(dto.getTitle())) {
            throw new IllegalArgumentException(MSG_TITLE_BLANK);
        }
        if (isBlank(dto.getModuleTitle())) {
            throw new IllegalArgumentException(MSG_MODULE_TITLE_BLANK);
        }
        if (dto.getCategory() == null) {
            throw new IllegalArgumentException(MSG_CATEGORY_NULL);
        }

        Integer weekly = dto.getWeeklyTimeLoad();
        if (weekly == null || weekly <= 0) {
            throw new IllegalArgumentException(MSG_WEEKLY_NULL_OR_LEQ_ZERO);
        }
        if (weekly > MAX_WEEKLY_MINUTES) {
            throw new IllegalArgumentException(String.format(MSG_WEEKLY_TOO_LARGE_TEMPLATE, MAX_WEEKLY_MINUTES));
        }

        switch (dto.getCategory()) {
            case EXAM -> {
                if (!(dto instanceof ExamTaskDTO exam)) {
                    throw new IllegalArgumentException(MSG_EXAM_REQUIRES_EXAM_DTO);
                }
                if (exam.getExamDate() == null) {
                    throw new IllegalArgumentException(MSG_EXAM_DATE_NULL);
                }
            }

            case SUBMISSION -> {
                if (!(dto instanceof SubmissionTaskDTO sub)) {
                    throw new IllegalArgumentException(MSG_SUBMISSION_REQUIRES_SUBMISSION_DTO);
                }
                if (sub.getSubmissionDay() == null) {
                    throw new IllegalArgumentException(MSG_SUBMISSION_DAY_NULL);
                }
                if (sub.getSubmissionTime() == null) {
                    throw new IllegalArgumentException(MSG_SUBMISSION_TIME_NULL);
                }
                if (sub.getSubmissionTime().isAfter(LATEST_TIME)) {
                    throw new IllegalArgumentException(MSG_SUBMISSION_TIME_TOO_LATE);
                }
            }

            case OTHER -> {
                if (!(dto instanceof OtherTaskDTO other)) {
                    throw new IllegalArgumentException(MSG_OTHER_REQUIRES_OTHER_DTO);
                }
                if (other.getStartDate() == null || other.getEndDate() == null) {
                    throw new IllegalArgumentException(MSG_OTHER_DATES_NULL);
                }
                if (other.getEndDate().isBefore(other.getStartDate())) {
                    throw new IllegalArgumentException(MSG_OTHER_END_BEFORE_START);
                }
            }

            default -> throw new IllegalArgumentException(MSG_UNKNOWN_TASK_CATEGORY);
        }
    }

    /**
     * DTO -> Entity.
     *
     * @param dto TaskDTO
     * @return Entity
     */
    private Task mapToEntity(TaskDTO dto) {
        return switch (dto.getCategory()) {
            case EXAM -> {
                ExamTaskDTO exam = (ExamTaskDTO) dto;
                yield new ExamTask(dto.getTitle(), dto.getWeeklyTimeLoad(), exam.getExamDate());
            }

            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;
                LocalDateTime deadline = computeNextSubmissionDeadline(sub.getSubmissionDay(), sub.getSubmissionTime());
                yield new SubmissionTask(dto.getTitle(), dto.getWeeklyTimeLoad(), deadline);
            }

            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;
                yield new OtherTask(
                        dto.getTitle(),
                        dto.getWeeklyTimeLoad(),
                        other.getStartDate().atStartOfDay(),
                        other.getEndDate().atTime(LATEST_TIME)
                );
            }

            default -> throw new IllegalArgumentException(MSG_UNKNOWN_TASK_CATEGORY);
        };
    }

    /**
     * Entity -> DTO.
     *
     * @param task Entity
     * @return passendes DTO
     */
    private TaskDTO mapToDto(Task task) {
        if (task == null) {
            return null;
        }

        String moduleTitle = (task.getModule() != null) ? task.getModule().getTitle() : null;

        if (task instanceof ExamTask exam) {
            return new ExamTaskDTO(
                    task.getTitle(),
                    moduleTitle,
                    task.getWeeklyDurationMinutes(),
                    DEFAULT_SEND_NOTIFICATION,
                    exam.getExamDate()
            );
        }

        if (task instanceof SubmissionTask sub) {
            LocalDateTime deadline = sub.getDeadline();

            Weekday day = (deadline != null) ? fromDayOfWeek(deadline.getDayOfWeek()) : null;
            LocalTime time = (deadline != null)
                    ? deadline.toLocalTime().withSecond(NORMALIZED_SECOND).withNano(NORMALIZED_NANO)
                    : null;

            return new SubmissionTaskDTO(
                    task.getTitle(),
                    moduleTitle,
                    task.getWeeklyDurationMinutes(),
                    DEFAULT_SEND_NOTIFICATION,
                    day,
                    time,
                    DEFAULT_SUBMISSION_CYCLE
            );
        }

        if (task instanceof OtherTask other) {
            LocalDate start = (other.getStartTime() != null) ? other.getStartTime().toLocalDate() : null;
            LocalDate end = (other.getEndTime() != null) ? other.getEndTime().toLocalDate() : null;

            return new OtherTaskDTO(
                    task.getTitle(),
                    moduleTitle,
                    task.getWeeklyDurationMinutes(),
                    DEFAULT_SEND_NOTIFICATION,
                    start,
                    end
            );
        }

        throw new IllegalArgumentException(String.format(MSG_UNKNOWN_TASK_TYPE_TEMPLATE, task.getClass().getSimpleName()));
    }

    private void requireExistingUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(MSG_USER_ID_NULL);
        }
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(MSG_USER_NOT_FOUND);
        }
    }

    private Module resolveModuleForUser(UUID userId, String moduleTitle) {
        return moduleRepository.findByUserId(userId).stream()
                .filter(m -> Objects.equals(m.getTitle(), moduleTitle))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(String.format(MSG_MODULE_NOT_FOUND_TEMPLATE, moduleTitle)));
    }

    private boolean userOwnsModule(UUID userId, UUID moduleId) {
        return moduleRepository.findByUserId(userId).stream()
                .anyMatch(m -> Objects.equals(m.getModuleId(), moduleId));
    }

    private Task findTaskByNaturalKey(UUID userId, UUID moduleId, String title, TaskCategory category) {
        List<Task> matches = taskRepository.findByModuleId(userId, moduleId).stream()
                .filter(t -> t.getCategory() == category)
                .filter(t -> Objects.equals(t.getTitle(), title))
                .toList();

        if (matches.isEmpty()) {
            throw new EntityNotFoundException(MSG_TASK_NOT_FOUND_GENERIC);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(MSG_TASK_DUPLICATE_TITLE);
        }
        return matches.get(0);
    }

    private static LocalDateTime computeNextSubmissionDeadline(Weekday weekday, LocalTime time) {
        LocalDate today = LocalDate.now();
        DayOfWeek target = toDayOfWeek(weekday);
        LocalDate next = nextOccurrence(today, target);
        LocalDateTime candidate = LocalDateTime.of(next, time);

        return candidate.isBefore(LocalDateTime.now()) ? candidate.plusWeeks(1) : candidate;
    }

    private static DayOfWeek toDayOfWeek(Weekday d) {
        return switch (d) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }

    private static Weekday fromDayOfWeek(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> Weekday.MONDAY;
            case TUESDAY -> Weekday.TUESDAY;
            case WEDNESDAY -> Weekday.WEDNESDAY;
            case THURSDAY -> Weekday.THURSDAY;
            case FRIDAY -> Weekday.FRIDAY;
            case SATURDAY -> Weekday.SATURDAY;
            case SUNDAY -> Weekday.SUNDAY;
        };
    }

    private static LocalDate nextOccurrence(LocalDate from, DayOfWeek target) {
        int current = from.getDayOfWeek().getValue();
        int wanted = target.getValue();
        int delta = (wanted - current + DAYS_PER_WEEK) % DAYS_PER_WEEK;
        return from.plusDays(delta);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}


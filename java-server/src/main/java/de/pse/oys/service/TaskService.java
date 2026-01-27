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

    private static final int MAX_WEEKLY_MINUTES = 7 * 24 * 60; // 10080
    private static final LocalTime LATEST_TIME = LocalTime.of(23, 59);

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
                    throw new IllegalArgumentException("Task-Typ kann nicht geändert werden.");
                }
                ExamTaskDTO examDto = (ExamTaskDTO) dto;
                exam.setExamDate(examDto.getExamDate());
            }

            case SUBMISSION -> {
                if (!(existing instanceof SubmissionTask sub)) {
                    throw new IllegalArgumentException("Task-Typ kann nicht geändert werden.");
                }
                SubmissionTaskDTO subDto = (SubmissionTaskDTO) dto;
                LocalDateTime deadline = computeNextSubmissionDeadline(subDto.getSubmissionDay(), subDto.getSubmissionTime());
                sub.setDeadline(deadline);
            }

            case OTHER -> {
                if (!(existing instanceof OtherTask other)) {
                    throw new IllegalArgumentException("Task-Typ kann nicht geändert werden.");
                }
                OtherTaskDTO otherDto = (OtherTaskDTO) dto;
                other.setStartTime(otherDto.getStartDate().atStartOfDay());
                other.setEndTime(otherDto.getEndDate().atTime(LATEST_TIME));
            }

            default -> throw new IllegalArgumentException("Unbekannte TaskCategory.");
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
            throw new IllegalArgumentException("taskId darf nicht null sein.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task nicht gefunden."));

        UUID moduleId = (task.getModule() != null) ? task.getModule().getModuleId() : null;
        if (moduleId == null || !userOwnsModule(userId, moduleId)) {
            throw new SecurityException("Kein Zugriff auf diese Aufgabe.");
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
            throw new IllegalArgumentException("TaskDTO darf nicht null sein.");
        }

        if (isBlank(dto.getTitle())) {
            throw new IllegalArgumentException("title darf nicht leer sein.");
        }
        if (isBlank(dto.getModuleTitle())) {
            throw new IllegalArgumentException("moduleTitle darf nicht leer sein.");
        }
        if (dto.getCategory() == null) {
            throw new IllegalArgumentException("category darf nicht null sein.");
        }

        Integer weekly = dto.getWeeklyTimeLoad();
        if (weekly == null || weekly <= 0) {
            throw new IllegalArgumentException("weeklyTimeLoad muss > 0 sein.");
        }
        if (weekly > MAX_WEEKLY_MINUTES) {
            throw new IllegalArgumentException("weeklyTimeLoad darf nicht größer als " + MAX_WEEKLY_MINUTES + " Minuten sein.");
        }

        switch (dto.getCategory()) {
            case EXAM -> {
                if (!(dto instanceof ExamTaskDTO exam)) {
                    throw new IllegalArgumentException("EXAM erfordert ExamTaskDTO.");
                }
                if (exam.getExamDate() == null) {
                    throw new IllegalArgumentException("examDate darf nicht null sein.");
                }
            }

            case SUBMISSION -> {
                if (!(dto instanceof SubmissionTaskDTO sub)) {
                    throw new IllegalArgumentException("SUBMISSION erfordert SubmissionTaskDTO.");
                }
                if (sub.getSubmissionDay() == null) {
                    throw new IllegalArgumentException("submissionDay darf nicht null sein.");
                }
                if (sub.getSubmissionTime() == null) {
                    throw new IllegalArgumentException("submissionTime darf nicht null sein.");
                }
                if (sub.getSubmissionTime().isAfter(LATEST_TIME)) {
                    throw new IllegalArgumentException("submissionTime darf nicht nach 23:59 sein.");
                }
            }

            case OTHER -> {
                if (!(dto instanceof OtherTaskDTO other)) {
                    throw new IllegalArgumentException("OTHER erfordert OtherTaskDTO.");
                }
                if (other.getStartDate() == null || other.getEndDate() == null) {
                    throw new IllegalArgumentException("startDate/endDate dürfen nicht null sein.");
                }
                if (other.getEndDate().isBefore(other.getStartDate())) {
                    throw new IllegalArgumentException("endDate muss >= startDate sein.");
                }
            }

            default -> throw new IllegalArgumentException("Unbekannte TaskCategory.");
        }
    }

    /**
     * DTO -> Entity.
     *
     * @param dto TaskDTO
     * @return Entity
     */
    private Task mapToEntity(TaskDTO dto) {
        UUID id = UUID.randomUUID();

        return switch (dto.getCategory()) {
            case EXAM -> {
                ExamTaskDTO exam = (ExamTaskDTO) dto;
                yield new ExamTask(id, dto.getTitle(), dto.getWeeklyTimeLoad(), exam.getExamDate());
            }

            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;
                LocalDateTime deadline = computeNextSubmissionDeadline(sub.getSubmissionDay(), sub.getSubmissionTime());
                yield new SubmissionTask(id, dto.getTitle(), dto.getWeeklyTimeLoad(), deadline);
            }

            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;
                yield new OtherTask(
                        id,
                        dto.getTitle(),
                        dto.getWeeklyTimeLoad(),
                        other.getStartDate().atStartOfDay(),
                        other.getEndDate().atTime(LATEST_TIME)
                );
            }

            default -> throw new IllegalArgumentException("Unbekannte TaskCategory.");
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

        boolean sendNotification = false;
        int submissionCycle = 1;

        if (task instanceof ExamTask exam) {
            return new ExamTaskDTO(
                    task.getTitle(),
                    moduleTitle,
                    task.getWeeklyDurationMinutes(),
                    sendNotification,
                    exam.getExamDate()
            );
        }

        if (task instanceof SubmissionTask sub) {
            LocalDateTime deadline = sub.getDeadline();

            Weekday day = (deadline != null) ? fromDayOfWeek(deadline.getDayOfWeek()) : null;
            LocalTime time = (deadline != null) ? deadline.toLocalTime().withSecond(0).withNano(0) : null;

            return new SubmissionTaskDTO(
                    task.getTitle(),
                    moduleTitle,
                    task.getWeeklyDurationMinutes(),
                    sendNotification,
                    day,
                    time,
                    submissionCycle
            );
        }

        if (task instanceof OtherTask other) {
            LocalDate start = (other.getStartTime() != null) ? other.getStartTime().toLocalDate() : null;
            LocalDate end = (other.getEndTime() != null) ? other.getEndTime().toLocalDate() : null;

            return new OtherTaskDTO(
                    task.getTitle(),
                    moduleTitle,
                    task.getWeeklyDurationMinutes(),
                    sendNotification,
                    start,
                    end
            );
        }

        throw new IllegalArgumentException("Unbekannter Task-Typ: " + task.getClass().getSimpleName());
    }

    private void requireExistingUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId darf nicht null sein.");
        }
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User nicht gefunden.");
        }
    }

    private Module resolveModuleForUser(UUID userId, String moduleTitle) {
        return moduleRepository.findByUserId(userId).stream()
                .filter(m -> Objects.equals(m.getTitle(), moduleTitle))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Modul nicht gefunden: " + moduleTitle));
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
            throw new EntityNotFoundException("Task nicht gefunden.");
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Mehrere Tasks mit gleichem Titel im Modul gefunden.");
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
        int delta = (wanted - current + 7) % 7;
        return from.plusDays(delta);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

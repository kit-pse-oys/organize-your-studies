package de.pse.oys.service;

import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.exceptions.AccessDeniedException;
import de.pse.oys.service.exceptions.ResourceNotFoundException;
import de.pse.oys.service.exceptions.ValidationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service für Task-Operationen. Kümmert sich um Validierung, Ownership-Checks und Persistenz.
 */
@Service
@Transactional
public class TaskService {

    private static final int MAX_WEEKLY_MINUTES = 7 * 24 * 60; // 10080
    private static final LocalTime LATEST_TIME = LocalTime.of(23, 59);

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
     * Erstellt einen Task für einen Nutzer in einem existierenden Modul.
     *
     * @param userId Nutzer-ID
     * @param dto    Task-Daten
     * @return ID des neu erstellten Tasks
     */
    public UUID createTask(UUID userId, TaskDTO dto) {
        validateData(userId, dto);

        Module module = findModuleOfUserByTitle(userId, dto.getModuleTitle());
        Task task = mapToEntity(dto);

        task.setModule(module);
        Task saved = taskRepository.save(task);
        return saved.getTaskId();
    }

    /**
     * Aktualisiert einen existierenden Task.
     *
     * <p>Ohne Task-ID im DTO muss der Task über einen stabilen Schlüssel identifiziert werden.
     * Hier wird (module + title + category) verwendet.</p>
     *
     * @param userId Nutzer-ID
     * @param dto    neue Task-Daten
     */
    public void updateTask(UUID userId, TaskDTO dto) {
        validateData(userId, dto);

        Module module = findModuleOfUserByTitle(userId, dto.getModuleTitle());
        Task existing = findTaskByNaturalKey(module.getModuleId(), dto.getTitle(), dto.getCategory());

        applyUpdates(existing, dto);
        taskRepository.save(existing);
    }

    /**
     * Löscht einen Task, sofern er dem Nutzer zugeordnet ist (über Modul → User).
     *
     * @param userId Nutzer-ID
     * @param taskId Task-ID
     */
    public void deleteTask(UUID userId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task nicht gefunden: " + taskId));

        Module module = task.getModule();
        if (module == null) {
            throw new ResourceNotFoundException("Task hat kein Modul gesetzt: " + taskId);
        }

        // Ownership: Modul muss dem Nutzer gehören
        Module persistedModule = moduleRepository.findById(module.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Modul nicht gefunden: " + module.getModuleId()));

        if (persistedModule.getUser() == null || !persistedModule.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Task gehört nicht zum Nutzer.");
        }

        taskRepository.deleteById(taskId);
    }

    /** Prüft Basisregeln (Existenz, Pflichtfelder, Grenzen, Kategorie-spezifische Constraints). */
    private void validateData(UUID userId, TaskDTO dto) {
        if (userId == null) throw new ValidationException("userId fehlt.");
        if (dto == null) throw new ValidationException("TaskDTO fehlt.");

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User nicht gefunden: " + userId);
        }

        if (isBlank(dto.getTitle())) throw new ValidationException("title fehlt.");
        if (isBlank(dto.getModuleTitle())) throw new ValidationException("moduleTitle fehlt.");
        if (dto.getCategory() == null) throw new ValidationException("category fehlt.");

        int weekly = dto.getWeeklyTimeLoad();
        if (weekly <= 0 || weekly > MAX_WEEKLY_MINUTES) {
            throw new ValidationException("weeklyTimeLoad muss in (0.." + MAX_WEEKLY_MINUTES + "] liegen.");
        }

        switch (dto.getCategory()) {
            case EXAM -> validateExam(dto);
            case SUBMISSION -> validateSubmission(dto);
            case OTHER -> validateOther(dto);
        }
    }

    private void validateExam(TaskDTO dto) {
        if (dto instanceof ExamTaskDTO exam) {
            if (exam.getExamDate() == null) {
                throw new ValidationException("examDate fehlt (EXAM).");
            }
        }
    }

    private void validateSubmission(TaskDTO dto) {
        if (dto instanceof SubmissionTaskDTO sub) {
            if (sub.getSubmissionDay() == null) throw new ValidationException("submissionDay fehlt (SUBMISSION).");
            if (sub.getSubmissionTime() == null) throw new ValidationException("submissionTime fehlt (SUBMISSION).");
            if (sub.getSubmissionTime().isAfter(LATEST_TIME)) {
                throw new ValidationException("submissionTime darf nicht nach 23:59 liegen.");
            }
            if (sub.getSubmissionCycle() == null || sub.getSubmissionCycle() <= 0) {
                throw new ValidationException("submissionCycle muss > 0 sein.");
            }
        }
    }

    private void validateOther(TaskDTO dto) {
        if (dto instanceof OtherTaskDTO other) {
            LocalDate start = other.getStartDate();
            LocalDate end = other.getEndDate();

            if (start == null) throw new ValidationException("startDate fehlt (OTHER).");
            if (end == null) throw new ValidationException("endDate fehlt (OTHER).");
            if (end.isBefore(start)) {
                throw new ValidationException("endDate darf nicht vor startDate liegen.");
            }
        }
    }

    /** Erstellt eine neue Task-Entity aus dem DTO (Kategorie entscheidet den Typ). */
    private Task mapToEntity(TaskDTO dto) {
        UUID id = UUID.randomUUID();

        return switch (dto.getCategory()) {
            case EXAM -> {
                ExamTaskDTO exam = (ExamTaskDTO) dto;
                ExamTask t = new ExamTask(id, dto.getTitle(), dto.getWeeklyTimeLoad());
                t.setFixedDeadline(exam.getExamDate());
                yield t;
            }
            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;
                OtherTask t = new OtherTask(id, dto.getTitle(), dto.getWeeklyTimeLoad());
                t.setTimeFrameStart(other.getStartDate());
                t.setTimeFrameEnd(other.getEndDate());
                yield t;
            }
            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;
                SubmissionTask t = new SubmissionTask(id, dto.getTitle(), dto.getWeeklyTimeLoad());

                LocalDate next = nextOccurrence(LocalDate.now(), toDayOfWeek(sub.getSubmissionDay()));
                t.setFixedDeadline(next);

                yield t;
            }
        };
    }

    /** Übernimmt neue Werte aus dem DTO in eine bestehende Entity (ohne Ownership-Wechsel). */
    private void applyUpdates(Task existing, TaskDTO dto) {
        existing.setTitle(dto.getTitle());
        existing.setWeeklyDurationMinutes(dto.getWeeklyTimeLoad());

        switch (dto.getCategory()) {
            case EXAM -> {
                if (dto instanceof ExamTaskDTO examDto && existing instanceof ExamTask ex) {
                    ex.setFixedDeadline(examDto.getExamDate());
                }
            }
            case OTHER -> {
                if (dto instanceof OtherTaskDTO otherDto && existing instanceof OtherTask ot) {
                    ot.setTimeFrameStart(otherDto.getStartDate());
                    ot.setTimeFrameEnd(otherDto.getEndDate());
                }
            }
            case SUBMISSION -> {
                if (dto instanceof SubmissionTaskDTO subDto && existing instanceof SubmissionTask st) {
                    LocalDate next = nextOccurrence(LocalDate.now(), toDayOfWeek(subDto.getSubmissionDay()));
                    st.setFixedDeadline(next);
                }
            }
        }
    }

    /** Liefert das Modul eines Nutzers anhand des Titels (case-insensitive). */
    private Module findModuleOfUserByTitle(UUID userId, String moduleTitle) {
        List<Module> modules = moduleRepository.findByUserId(userId);

        return modules.stream()
                .filter(m -> m.getTitle() != null && m.getTitle().equalsIgnoreCase(moduleTitle))
                .min(Comparator.comparing(Module::getTitle))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Modul nicht gefunden (userId=" + userId + ", title=" + moduleTitle + ")"));
    }

    /** Findet einen Task anhand (moduleId + title + category). */
    private Task findTaskByNaturalKey(UUID moduleId, String title, TaskCategory category) {
        List<Task> tasks = taskRepository.findByModuleId(moduleId);

        return tasks.stream()
                .filter(t -> t.getTitle() != null && t.getTitle().equalsIgnoreCase(title))
                .filter(t -> t.getCategory() == category)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task nicht gefunden (moduleId=" + moduleId + ", title=" + title + ", category=" + category + ")"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Liefert das nächste Datum (inklusive {@code from}), an dem {@code target} vorkommt.
     *
     * @param from   Startdatum
     * @param target gewünschter Wochentag
     * @return nächstes passendes Datum
     */
    private static LocalDate nextOccurrence(LocalDate from, DayOfWeek target) {
        int delta = (target.getValue() - from.getDayOfWeek().getValue() + 7) % 7;
        return from.plusDays(delta);
    }

    /**
     * Konvertiert RecurringDay (DB/DTO) in DayOfWeek (java.time).
     *
     * @param day Wochentag im RecurringDay-Format
     * @return entsprechender DayOfWeek
     */
    private static DayOfWeek toDayOfWeek(RecurringDay day) {
        return switch (day) {
            case MON -> DayOfWeek.MONDAY;
            case TUE -> DayOfWeek.TUESDAY;
            case WED -> DayOfWeek.WEDNESDAY;
            case THU -> DayOfWeek.THURSDAY;
            case FRI -> DayOfWeek.FRIDAY;
            case SAT -> DayOfWeek.SATURDAY;
            case SUN -> DayOfWeek.SUNDAY;
        };
    }
}

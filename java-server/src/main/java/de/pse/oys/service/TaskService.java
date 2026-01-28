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
import de.pse.oys.dto.InvalidDtoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
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
 * Dieser Service kümmert sich um alles, was mit Aufgaben (Tasks) zu tun hat.
 * Er verwaltet das Erstellen, Ändern, Löschen und Abrufen von Aufgaben für die Nutzer.
 *
 * @author uqvfm
 * @version 1.0
 */
@Service
@Transactional
public class TaskService {

    private static final int DAYS_PER_WEEK = 7;
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int LAST_HOUR_OF_DAY = 23;
    private static final int LAST_MINUTE_OF_DAY = 59;
    private static final int MAX_WEEKLY_MINUTES = DAYS_PER_WEEK * HOURS_PER_DAY * MINUTES_PER_HOUR;
    private static final LocalTime LATEST_TIME = LocalTime.of(LAST_HOUR_OF_DAY, LAST_MINUTE_OF_DAY);
    private static final boolean DEFAULT_SEND_NOTIFICATION = false;
    private static final int DEFAULT_SUBMISSION_CYCLE = 1;

    private static final String METHOD_GET_ID = "getId";
    private static final String METHOD_GET_TASK_ID = "getTaskId";
    private static final String MSG_USER_ID_NULL = "userId darf nicht null sein.";
    private static final String MSG_USER_NOT_FOUND = "User nicht gefunden.";
    private static final String MSG_TASK_DTO_NULL = "TaskDTO darf nicht null sein.";
    private static final String MSG_TITLE_BLANK = "title darf nicht leer sein.";
    private static final String MSG_MODULE_TITLE_BLANK = "moduleTitle darf nicht leer sein.";
    private static final String MSG_CATEGORY_NULL = "category darf nicht null sein.";
    private static final String MSG_WEEKLY_NULL_OR_LEQ_ZERO = "weeklyTimeLoad muss > 0 sein.";
    private static final String MSG_WEEKLY_TOO_LARGE_TEMPLATE = "weeklyTimeLoad darf nicht größer als %d Minuten sein.";
    private static final String MSG_EXAM_REQUIRES_EXAM_DTO = "EXAM erfordert ExamTaskDTO.";
    private static final String MSG_EXAM_DATE_NULL = "examDate darf nicht null sein.";
    private static final String MSG_REFLECTION_ID_ERROR = "Unerwarteter Fehler beim Auslesen der ID via Reflection";
    private static final String MSG_SUBMISSION_REQUIRES_SUBMISSION_DTO = "SUBMISSION erfordert SubmissionTaskDTO.";
    private static final String MSG_SUBMISSION_DAY_NULL = "submissionDay darf nicht null sein.";
    private static final String MSG_SUBMISSION_TIME_NULL = "submissionTime darf nicht null sein.";
    private static final String MSG_SUBMISSION_TIME_TOO_LATE = "submissionTime darf nicht nach 23:59 sein.";
    private static final String MSG_OTHER_REQUIRES_OTHER_DTO = "OTHER erfordert OtherTaskDTO.";
    private static final String MSG_OTHER_DATES_NULL = "startDate/endDate dürfen nicht null sein.";
    private static final String MSG_OTHER_END_BEFORE_START = "endDate muss >= startDate sein.";
    private static final String MSG_UNKNOWN_TASK_CATEGORY = "Unbekannte TaskCategory.";
    private static final String MSG_TYPE_CHANGE_NOT_ALLOWED = "Task-Typ kann nicht geändert werden.";
    private static final String MSG_TASK_ID_NULL = "taskId darf nicht null sein.";
    private static final String MSG_TASK_NOT_FOUND = "Task nicht gefunden.";
    private static final String MSG_ACCESS_DENIED = "Kein Zugriff auf diese Aufgabe.";
    private static final String MSG_MODULE_NOT_FOUND_TEMPLATE = "Modul nicht gefunden: %s";
    private static final String MSG_TASK_DUPLICATE_TITLE = "Mehrere Tasks mit gleichem Titel im Modul gefunden.";
    private static final String MSG_UNKNOWN_TASK_TYPE_TEMPLATE = "Unbekannter Task-Typ: %s";

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final TaskRepository taskRepository;

    /**
     * Erstellt den TaskService mit den nötigen Datenbank-Anbindungen.
     */
    public TaskService(UserRepository userRepository, ModuleRepository moduleRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Legt eine neue Aufgabe für einen Nutzer an.
     * Dabei wird geprüft, ob der Nutzer existiert und die Daten gültig sind.
     */
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
     * Aktualisiert eine bereits bestehende Aufgabe.
     * Ein Wechsel der Kategorie (z.B. von Prüfung zu Abgabe) ist dabei nicht erlaubt.
     */
    public TaskDTO updateTask(UUID userId, TaskDTO dto) {
        requireExistingUser(userId);
        validateData(dto);

        Module module = resolveModuleForUser(userId, dto.getModuleTitle());
        Task existing = findExistingForUpdate(userId, module.getModuleId(), dto);
        Task mapped = mapToEntity(dto);

        if (existing.getCategory() != mapped.getCategory()) {
            throw new IllegalArgumentException(MSG_TYPE_CHANGE_NOT_ALLOWED);
        }

        existing.setTitle(mapped.getTitle());
        existing.setWeeklyDurationMinutes(mapped.getWeeklyDurationMinutes());
        existing.setModule(module);

        switch (existing.getCategory()) {
            case EXAM -> {
                ExamTask ex = (ExamTask) existing;
                ExamTask mx = (ExamTask) mapped;
                ex.setExamDate(mx.getExamDate());
            }
            case SUBMISSION -> {
                SubmissionTask ex = (SubmissionTask) existing;
                SubmissionTask mx = (SubmissionTask) mapped;
                ex.setDeadline(mx.getDeadline());
            }
            case OTHER -> {
                OtherTask ex = (OtherTask) existing;
                OtherTask mx = (OtherTask) mapped;
                ex.setStartTime(mx.getStartTime());
                ex.setEndTime(mx.getEndTime());
            }
            default -> throw new IllegalStateException(String.format(
                    MSG_UNKNOWN_TASK_TYPE_TEMPLATE, existing.getCategory()));
        }

        Task saved = taskRepository.save(existing);
        return mapToDto(saved);
    }

    /**
     * Löscht eine Aufgabe aus dem System, sofern sie dem Nutzer gehört.
     */
    public void deleteTask(UUID userId, UUID taskId) {
        requireExistingUser(userId);
        if (taskId == null) throw new IllegalArgumentException(MSG_TASK_ID_NULL);
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException(MSG_TASK_NOT_FOUND));
        UUID moduleId = (task.getModule() != null) ? task.getModule().getModuleId() : null;
        if (moduleId == null || !userOwnsModule(userId, moduleId)) throw new SecurityException(MSG_ACCESS_DENIED);
        taskRepository.delete(task);
    }

    /**
     * Listet alle Aufgaben auf, die zu einem bestimmten Nutzer gehören, sortiert nach Modul und Titel.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(UUID userId) {
        requireExistingUser(userId);
        List<Module> modules = moduleRepository.findByUserId(userId);
        List<TaskDTO> result = new ArrayList<>();
        for (Module module : modules) {
            taskRepository.findByModuleId(userId, module.getModuleId()).forEach(task -> result.add(mapToDto(task)));
        }
        result.sort(Comparator.comparing(TaskDTO::getModuleTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(TaskDTO::getTitle, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    /**
     * Prüft, ob die eingegebenen Daten einer Aufgabe vollständig und logisch korrekt sind.
     */
    private void validateData(TaskDTO dto) {
        if (dto == null) throw new InvalidDtoException(MSG_TASK_DTO_NULL);
        if (isBlank(dto.getTitle())) throw new InvalidDtoException(MSG_TITLE_BLANK);
        if (isBlank(dto.getModuleTitle())) throw new InvalidDtoException(MSG_MODULE_TITLE_BLANK);
        if (dto.getCategory() == null) throw new InvalidDtoException(MSG_CATEGORY_NULL);

        Integer weekly = dto.getWeeklyTimeLoad();
        if (weekly == null || weekly <= 0) throw new InvalidDtoException(MSG_WEEKLY_NULL_OR_LEQ_ZERO);
        if (weekly > MAX_WEEKLY_MINUTES) throw new InvalidDtoException(String.format(MSG_WEEKLY_TOO_LARGE_TEMPLATE, MAX_WEEKLY_MINUTES));

        switch (dto.getCategory()) {
            case EXAM -> {
                if (!(dto instanceof ExamTaskDTO exam)) throw new InvalidDtoException(MSG_EXAM_REQUIRES_EXAM_DTO);
                if (exam.getExamDate() == null) throw new InvalidDtoException(MSG_EXAM_DATE_NULL);
            }
            case SUBMISSION -> {
                if (!(dto instanceof SubmissionTaskDTO sub)) throw new InvalidDtoException(MSG_SUBMISSION_REQUIRES_SUBMISSION_DTO);
                if (sub.getSubmissionDay() == null) throw new InvalidDtoException(MSG_SUBMISSION_DAY_NULL);
                if (sub.getSubmissionTime() == null) throw new InvalidDtoException(MSG_SUBMISSION_TIME_NULL);
                if (sub.getSubmissionTime().isAfter(LATEST_TIME)) throw new InvalidDtoException(MSG_SUBMISSION_TIME_TOO_LATE);
            }
            case OTHER -> {
                if (!(dto instanceof OtherTaskDTO other)) throw new InvalidDtoException(MSG_OTHER_REQUIRES_OTHER_DTO);
                if (other.getStartDate() == null || other.getEndDate() == null) throw new InvalidDtoException(MSG_OTHER_DATES_NULL);
                if (other.getEndDate().isBefore(other.getStartDate())) throw new InvalidDtoException(MSG_OTHER_END_BEFORE_START);
            }
            default -> throw new InvalidDtoException(MSG_UNKNOWN_TASK_CATEGORY);
        }
    }

    /**
     * Verwandelt ein Daten-Objekt (DTO) in eine echte Datenbank-Entity.
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
                yield new OtherTask(dto.getTitle(), dto.getWeeklyTimeLoad(), other.getStartDate().atStartOfDay(), other.getEndDate().atTime(LATEST_TIME));
            }
        };
    }

    /**
     * Verwandelt eine Datenbank-Entity zurück in ein DTO für die Benutzeroberfläche.
     */
    private TaskDTO mapToDto(Task task) {
        if (task == null) return null;
        String moduleTitle = (task.getModule() != null) ? task.getModule().getTitle() : null;

        return switch (task.getCategory()) {
            case EXAM -> {
                ExamTask exam = (ExamTask) task;
                yield new ExamTaskDTO(task.getTitle(), moduleTitle, task.getWeeklyDurationMinutes(), DEFAULT_SEND_NOTIFICATION, exam.getExamDate());
            }
            case SUBMISSION -> {
                SubmissionTask sub = (SubmissionTask) task;
                LocalDateTime dl = sub.getDeadline();
                yield new SubmissionTaskDTO(task.getTitle(), moduleTitle, task.getWeeklyDurationMinutes(), DEFAULT_SEND_NOTIFICATION,
                        dl != null ? fromDayOfWeek(dl.getDayOfWeek()) : null,
                        dl != null ? dl.toLocalTime().withSecond(0).withNano(0) : null,
                        DEFAULT_SUBMISSION_CYCLE);
            }
            case OTHER -> {
                OtherTask other = (OtherTask) task;
                yield new OtherTaskDTO(task.getTitle(), moduleTitle, task.getWeeklyDurationMinutes(), DEFAULT_SEND_NOTIFICATION,
                        other.getStartTime() != null ? other.getStartTime().toLocalDate() : null,
                        other.getEndTime() != null ? other.getEndTime().toLocalDate() : null);
            }
        };
    }

    /**
     * Stellt sicher, dass ein Nutzer wirklich im System vorhanden ist.
     */
    private void requireExistingUser(UUID userId) {
        if (userId == null) throw new IllegalArgumentException(MSG_USER_ID_NULL);
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(MSG_USER_NOT_FOUND));
    }

    /**
     * Sucht das passende Modul eines Nutzers anhand seines Namens heraus.
     */
    private Module resolveModuleForUser(UUID userId, String moduleTitle) {
        return moduleRepository.findByUserId(userId).stream()
                .filter(m -> Objects.equals(m.getTitle(), moduleTitle))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(String.format(MSG_MODULE_NOT_FOUND_TEMPLATE, moduleTitle)));
    }

    /**
     * Überprüft, ob ein Modul tatsächlich zu einem bestimmten Nutzer gehört.
     */
    private boolean userOwnsModule(UUID userId, UUID moduleId) {
        return moduleRepository.findByUserId(userId).stream().anyMatch(m -> Objects.equals(m.getModuleId(), moduleId));
    }

    /**
     * Findet die richtige Aufgabe in der Datenbank, um sie zu bearbeiten.
     */
    private Task findExistingForUpdate(UUID userId, UUID moduleId, TaskDTO dto) {
        UUID id = tryExtractId(dto);
        if (id != null) {
            Task byId = taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(MSG_TASK_NOT_FOUND));
            UUID mid = (byId.getModule() != null) ? byId.getModule().getModuleId() : null;
            if (mid == null || !userOwnsModule(userId, mid)) throw new SecurityException(MSG_ACCESS_DENIED);
            return byId;
        }
        return taskRepository.findByModuleId(userId, moduleId).stream()
                .filter(t -> t.getCategory() == dto.getCategory())
                .filter(t -> Objects.equals(t.getTitle(), dto.getTitle()))
                .reduce((a, b) -> { throw new IllegalStateException(MSG_TASK_DUPLICATE_TITLE); })
                .orElseThrow(() -> new EntityNotFoundException(MSG_TASK_NOT_FOUND));
    }

    /**
     * Versucht, eine ID aus dem DTO zu fischen (egal ob sie 'id' oder 'taskId' heißt).
     */
    private UUID tryExtractId(Object dto) {
        UUID id = tryInvokeUuidGetter(dto, METHOD_GET_ID);
        return (id != null) ? id : tryInvokeUuidGetter(dto, METHOD_GET_TASK_ID);
    }

    /**
     * Hilfsmethode, um per Reflection eine ID-Getter-Methode aufzurufen.
     */
    private UUID tryInvokeUuidGetter(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object val = m.invoke(target);
            return (val instanceof UUID) ? (UUID) val : null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(MSG_REFLECTION_ID_ERROR, e);
        }
    }

    /**
     * Berechnet den nächsten fälligen Termin für eine Abgabe.
     */
    private static LocalDateTime computeNextSubmissionDeadline(Weekday weekday, LocalTime time) {
        LocalDate today = LocalDate.now();
        LocalDateTime candidate = LocalDateTime.of(nextOccurrence(today, toDayOfWeek(weekday)), time);
        return candidate.isBefore(LocalDateTime.now()) ? candidate.plusWeeks(1) : candidate;
    }

    private static DayOfWeek toDayOfWeek(Weekday d) {
        return DayOfWeek.valueOf(d.name());
    }

    private static Weekday fromDayOfWeek(DayOfWeek d) {
        return Weekday.valueOf(d.name());
    }

    private static LocalDate nextOccurrence(LocalDate from, DayOfWeek target) {
        int delta = (target.getValue() - from.getDayOfWeek().getValue() + DAYS_PER_WEEK) % DAYS_PER_WEEK;
        return from.plusDays(delta);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
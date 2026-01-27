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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
 * Service-Klasse zur Verwaltung von Aufgaben.
 * Verarbeitet die Geschäftslogik für das Erstellen, Aktualisieren,
 * Löschen und Abfragen von Tasks.
 *
 * @author utgid
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
     * Erzeugt eine neue Instanz des TaskService.
     *
     * @param userRepository   Repository für Benutzerdaten
     * @param moduleRepository Repository für Moduldaten
     * @param taskRepository   Repository für Aufgabendaten
     */
    public TaskService(UserRepository userRepository,
                       ModuleRepository moduleRepository,
                       TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Erstellt eine neue Aufgabe für einen Benutzer.
     *
     * @param userId Die UUID des authentifizierten Benutzers
     * @param dto    Die Daten der zu erstellenden Aufgabe
     * @return Das erstellte TaskDTO
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
     * Aktualisiert eine bestehende Aufgabe.
     *
     * @param userId Die UUID des Benutzers (Ownership-Check)
     * @param dto    Die neuen Daten der Aufgabe
     * @return Das aktualisierte TaskDTO
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
                ExamTaskDTO examDto = (ExamTaskDTO) dto;
                ((ExamTask) existing).setExamDate(examDto.getExamDate());
            }
            case SUBMISSION -> {
                SubmissionTaskDTO subDto = (SubmissionTaskDTO) dto;
                LocalDateTime deadline = computeNextSubmissionDeadline(
                        subDto.getSubmissionDay(),
                        subDto.getSubmissionTime()
                );
                ((SubmissionTask) existing).setDeadline(deadline);
            }
            case OTHER -> {
                OtherTaskDTO otherDto = (OtherTaskDTO) dto;
                ((OtherTask) existing).setStartTime(otherDto.getStartDate().atStartOfDay());
                ((OtherTask) existing).setEndTime(otherDto.getEndDate().atTime(LATEST_TIME));
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unbekannte TaskCategory");
        }

        Task saved = taskRepository.save(existing);
        return mapToDto(saved);
    }

    /**
     * Löscht eine Aufgabe aus dem System.
     *
     * @param userId Die UUID des Benutzers
     * @param taskId Die UUID der zu löschenden Aufgabe
     */
    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        requireExistingUser(userId);

        if (taskId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId darf nicht null sein");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task nicht gefunden"));

        UUID moduleId = task.getModule().getModuleId();
        if (!userOwnsModule(userId, moduleId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf diese Aufgabe");
        }

        taskRepository.delete(task);
    }

    /**
     * Ruft alle Aufgaben ab, die einem bestimmten Benutzer zugeordnet sind.
     *
     * @param userId Die UUID des authentifizierten Benutzers
     * @return Liste aller Tasks des Benutzers als DTOs
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

        result.sort(Comparator
                .comparing(TaskDTO::getModuleTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(TaskDTO::getTitle, String.CASE_INSENSITIVE_ORDER));

        return result;
    }

    /**
     * Validiert die Eingabedaten eines TaskDTOs.
     *
     * @param dto Das zu prüfende DTO
     */
    private void validateData(TaskDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TaskDTO darf nicht null sein");
        }

        if (isBlank(dto.getTitle()) || isBlank(dto.getModuleTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titel und Modul dürfen nicht leer sein");
        }

        if (dto.getWeeklyTimeLoad() == null || dto.getWeeklyTimeLoad() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weeklyTimeLoad muss > 0 sein");
        }

        if (dto.getWeeklyTimeLoad() > MAX_WEEKLY_MINUTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weeklyTimeLoad zu groß");
        }

        switch (dto.getCategory()) {
            case EXAM -> {
                if (!(((ExamTaskDTO) dto).getExamDate() != null)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "examDate fehlt");
                }
            }
            case SUBMISSION -> {
                SubmissionTaskDTO sub = (SubmissionTaskDTO) dto;
                if (sub.getSubmissionTime().isAfter(LATEST_TIME)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "submissionTime > 23:59");
                }
            }
            case OTHER -> {
                OtherTaskDTO other = (OtherTaskDTO) dto;
                if (other.getEndDate().isBefore(other.getStartDate())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate < startDate");
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unbekannte TaskCategory");
        }
    }

    /**
     * Überführt ein TaskDTO in eine Task-Entität.
     *
     * @param dto Das Quelldaten-Objekt
     * @return Die vorbereitete Task-Entität
     */
    private Task mapToEntity(TaskDTO dto) {
        UUID id = UUID.randomUUID();

        return switch (dto.getCategory()) {
            case EXAM -> new ExamTask(id, dto.getTitle(), dto.getWeeklyTimeLoad(),
                    ((ExamTaskDTO) dto).getExamDate());
            case SUBMISSION -> new SubmissionTask(id, dto.getTitle(), dto.getWeeklyTimeLoad(),
                    computeNextSubmissionDeadline(
                            ((SubmissionTaskDTO) dto).getSubmissionDay(),
                            ((SubmissionTaskDTO) dto).getSubmissionTime()
                    ));
            case OTHER -> new OtherTask(id, dto.getTitle(), dto.getWeeklyTimeLoad(),
                    ((OtherTaskDTO) dto).getStartDate().atStartOfDay(),
                    ((OtherTaskDTO) dto).getEndDate().atTime(LATEST_TIME));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unbekannte TaskCategory");
        };
    }

    /**
     * Prüft, ob ein Benutzer existiert.
     */
    private void requireExistingUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User nicht gefunden");
        }
    }

    /**
     * Ermittelt ein Modul eines Benutzers anhand des Titels.
     */
    private Module resolveModuleForUser(UUID userId, String moduleTitle) {
        return moduleRepository.findByUserId(userId).stream()
                .filter(m -> Objects.equals(m.getTitle(), moduleTitle))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modul nicht gefunden"));
    }

    /**
     * Prüft, ob ein Modul dem Benutzer gehört.
     */
    private boolean userOwnsModule(UUID userId, UUID moduleId) {
        return moduleRepository.findByUserId(userId).stream()
                .anyMatch(m -> Objects.equals(m.getModuleId(), moduleId));
    }

    /**
     * Findet einen Task über natürlichen Schlüssel (Modul, Titel, Kategorie).
     */
    private Task findTaskByNaturalKey(UUID userId, UUID moduleId, String title, TaskCategory category) {
        List<Task> matches = taskRepository.findByModuleId(userId, moduleId).stream()
                .filter(t -> t.getCategory() == category)
                .filter(t -> Objects.equals(t.getTitle(), title))
                .toList();

        if (matches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task nicht gefunden");
        }
        if (matches.size() > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mehrdeutiger Task");
        }
        return matches.get(0);
    }

    /**
     * Berechnet den nächsten Abgabetermin für einen wöchentlichen Submission-Task.
     */
    private static LocalDateTime computeNextSubmissionDeadline(Weekday weekday, LocalTime time) {
        LocalDate today = LocalDate.now();
        DayOfWeek target = toDayOfWeek(weekday);
        LocalDate next = nextOccurrence(today, target);
        LocalDateTime candidate = LocalDateTime.of(next, time);

        return candidate.isBefore(LocalDateTime.now()) ? candidate.plusWeeks(1) : candidate;
    }

    /**
     * Konvertiert Weekday nach DayOfWeek.
     */
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

    /**
     * Liefert das nächste Datum für einen Wochentag.
     */
    private static LocalDate nextOccurrence(LocalDate from, DayOfWeek target) {
        int current = from.getDayOfWeek().getValue();
        int wanted = target.getValue();
        int delta = (wanted - current + 7) % 7;
        return from.plusDays(delta);
    }

    /**
     * Prüft, ob ein String leer ist.
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

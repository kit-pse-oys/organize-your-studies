package de.pse.oys.task;

import de.pse.oys.domain.Module;
import de.pse.oys.domain.OtherTask;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.Weekday;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.TaskService;
import de.pse.oys.dto.InvalidDtoException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den TaskService.
 *
 * @author uqvfm
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {

    private static final String MODULE_TITLE = "Mathe 2";
    private static final String TITLE = "Aufgabe";
    private static final int MAX_WEEKLY_MINUTES = 7 * 24 * 60;

    @Mock private UserRepository userRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private TaskRepository taskRepository;

    private TaskService service;

    private UUID userId;
    private UUID moduleId;

    private User user;
    private Module module;

    @BeforeEach
    void setUp() {
        service = new TaskService(userRepository, moduleRepository, taskRepository);

        userId = UUID.randomUUID();
        moduleId = UUID.randomUUID();

        user = mock(User.class);
        module = mock(Module.class);

        lenient().when(module.getModuleId()).thenReturn(moduleId);
        lenient().when(module.getTitle()).thenReturn(MODULE_TITLE);

        // Standard: User existiert
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Standard: User hat das Modul "Mathe 2"
        lenient().when(moduleRepository.findByUserId(userId)).thenReturn(List.of(module));

        // Standard: save gibt Argument zurück
        lenient().when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // createTask(...)
    // -------------------------------------------------------------------------

    @Test
    void createTask_saves_whenValidOtherTask() {
        OtherTaskDTO dto = mock(OtherTaskDTO.class);
        stubBase(dto, TITLE, MODULE_TITLE, TaskCategory.OTHER, 60);

        when(dto.getStartDate()).thenReturn(LocalDate.of(2026, 1, 10));
        when(dto.getEndDate()).thenReturn(LocalDate.of(2026, 1, 10));

        TaskDTO result = assertDoesNotThrow(() -> service.createTask(userId, dto));

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_throwsEntityNotFound_whenModuleNotFoundForUser() {
        when(moduleRepository.findByUserId(userId)).thenReturn(List.of()); // kein Modul

        OtherTaskDTO dto = mock(OtherTaskDTO.class);
        stubBase(dto, TITLE, "GibtEsNicht", TaskCategory.OTHER, 60);
        when(dto.getStartDate()).thenReturn(LocalDate.of(2026, 1, 10));
        when(dto.getEndDate()).thenReturn(LocalDate.of(2026, 1, 11));

        assertThrows(EntityNotFoundException.class, () -> service.createTask(userId, dto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_throwsInvalidDtoException_whenWeeklyTimeLoadTooHigh() {
        OtherTaskDTO dto = mock(OtherTaskDTO.class);
        stubBase(dto, TITLE, MODULE_TITLE, TaskCategory.OTHER, MAX_WEEKLY_MINUTES + 1);

        when(dto.getStartDate()).thenReturn(LocalDate.of(2026, 1, 10));
        when(dto.getEndDate()).thenReturn(LocalDate.of(2026, 1, 11));

        assertThrows(InvalidDtoException.class, () -> service.createTask(userId, dto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_throwsInvalidDtoException_whenOtherTaskEndBeforeStart() {
        OtherTaskDTO dto = mock(OtherTaskDTO.class);
        stubBase(dto, TITLE, MODULE_TITLE, TaskCategory.OTHER, 60);

        when(dto.getStartDate()).thenReturn(LocalDate.of(2026, 2, 2));
        when(dto.getEndDate()).thenReturn(LocalDate.of(2026, 2, 1));

        assertThrows(InvalidDtoException.class, () -> service.createTask(userId, dto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_throwsInvalidDtoException_whenSubmissionTimeAfter2359() {
        SubmissionTaskDTO dto = mock(SubmissionTaskDTO.class);
        stubBase(dto, "Abgabe", MODULE_TITLE, TaskCategory.SUBMISSION, 30);

        when(dto.getSubmissionDay()).thenReturn(Weekday.MONDAY);
        when(dto.getSubmissionTime()).thenReturn(LocalTime.of(23, 59, 59)); // > 23:59
        when(dto.getSubmissionCycle()).thenReturn(1);

        assertThrows(InvalidDtoException.class, () -> service.createTask(userId, dto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_saves_whenValidExamTask() {
        ExamTaskDTO dto = mock(ExamTaskDTO.class);
        stubBase(dto, "Klausur", MODULE_TITLE, TaskCategory.EXAM, 120);

        when(dto.getExamDate()).thenReturn(LocalDate.of(2026, 2, 20));

        TaskDTO result = assertDoesNotThrow(() -> service.createTask(userId, dto));

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // -------------------------------------------------------------------------
    // updateTask(...)
    // -------------------------------------------------------------------------

    @Test
    void updateTask_saves_whenValidOtherTaskUpdate_naturalKey() {
        OtherTaskDTO dto = mock(OtherTaskDTO.class);
        stubBase(dto, "Update", MODULE_TITLE, TaskCategory.OTHER, 45);

        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 2);

        when(dto.getStartDate()).thenReturn(start);
        when(dto.getEndDate()).thenReturn(end);

        OtherTask existing = mock(OtherTask.class);
        when(existing.getCategory()).thenReturn(TaskCategory.OTHER);
        when(existing.getTitle()).thenReturn("Update");
        when(existing.getModule()).thenReturn(module);

        when(taskRepository.findByModuleId(userId, moduleId)).thenReturn(List.of(existing));

        TaskDTO result = assertDoesNotThrow(() -> service.updateTask(userId, dto));
        assertNotNull(result);

        // Speichert bestehende Entity
        verify(taskRepository, times(1)).save(existing);

        // Übernahme der Felder aus dem Mapping (common fields)
        verify(existing).setTitle("Update");
        verify(existing).setWeeklyDurationMinutes(45);
        verify(existing).setModule(module);

        // OTHER-spezifisch: Start/Ende aus Mapping
        verify(existing).setStartTime(start.atStartOfDay());
        verify(existing).setEndTime(end.atTime(LocalTime.of(23, 59)));
    }


    @Test
    void updateTask_throwsEntityNotFound_whenNoMatchingTaskFound() {
        OtherTaskDTO dto = mock(OtherTaskDTO.class);
        stubBase(dto, "Update", MODULE_TITLE, TaskCategory.OTHER, 45);

        when(dto.getStartDate()).thenReturn(LocalDate.of(2026, 3, 1));
        when(dto.getEndDate()).thenReturn(LocalDate.of(2026, 3, 2));

        when(taskRepository.findByModuleId(userId, moduleId)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> service.updateTask(userId, dto));
        verify(taskRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteTask(...)
    // -------------------------------------------------------------------------

    @Test
    void deleteTask_deletes_whenUserOwnsModule() {
        UUID taskId = UUID.randomUUID();

        Task task = mock(Task.class);
        when(task.getModule()).thenReturn(module);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> service.deleteTask(userId, taskId));

        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_throwsSecurityException_whenUserDoesNotOwnModule() {
        UUID taskId = UUID.randomUUID();

        Task task = mock(Task.class);
        when(task.getModule()).thenReturn(module);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        when(moduleRepository.findByUserId(userId)).thenReturn(List.of()); // User besitzt Modul nicht

        assertThrows(SecurityException.class, () -> service.deleteTask(userId, taskId));
        verify(taskRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void stubBase(TaskDTO dto,
                          String title,
                          String moduleTitle,
                          TaskCategory category,
                          Integer weeklyTimeLoad) {
        when(dto.getTitle()).thenReturn(title);
        when(dto.getModuleTitle()).thenReturn(moduleTitle);
        when(dto.getCategory()).thenReturn(category);
        when(dto.getWeeklyTimeLoad()).thenReturn(weeklyTimeLoad);
    }
}

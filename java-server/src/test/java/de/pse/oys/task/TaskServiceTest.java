package de.pse.oys.task;

import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.OtherTask;
import de.pse.oys.domain.SubmissionTask;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.TaskService;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den TaskService.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OLD_MODULE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID NEW_MODULE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final String OLD_MODULE_TITLE = "Old Module";
    private static final String NEW_MODULE_TITLE = "New Module";

    private static final int WEEKLY_LOAD = 120;

    private static final LocalDateTime T0 = LocalDateTime.of(2030, 1, 1, 12, 0);
    private static final LocalDateTime OTHER_START = T0.plusDays(1);
    private static final LocalDateTime OTHER_END = T0.plusDays(2);
    private static final LocalDate EXAM_DATE = LocalDate.of(2030, 1, 15);

    @Mock private UserRepository userRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private TaskRepository taskRepository;

    private TaskService sut;

    @BeforeEach
    void setUp() {
        sut = new TaskService(userRepository, moduleRepository, taskRepository);
    }

    // ------------------------------------------------------------
    // getTasksByUserId
    // ------------------------------------------------------------
    @Nested
    @DisplayName("getTasksByUserId")
    class GetTasksByUserIdTests {

        @Test
        @DisplayName("wirft ResourceNotFoundException, wenn User nicht existiert")
        void throwsWhenUserDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> sut.getTasksByUserId(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).findAllByModuleUserUserId(any());
        }

        @Test
        @DisplayName("liefert gemappte DTOs für alle Tasks des Users als WrapperDTO-Liste")
        void returnsMappedDtos() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = new Module(OLD_MODULE_TITLE, anyPriority());
            ExamTask examTask = new ExamTask("Exam", WEEKLY_LOAD, EXAM_DATE);
            module.addTask(examTask);

            when(taskRepository.findAllByModuleUserUserId(USER_ID)).thenReturn(List.of(examTask));

            List<WrapperDTO<TaskDTO>> result = sut.getTasksByUserId(USER_ID);

            assertThat(result).hasSize(1);

            WrapperDTO<TaskDTO> wrapper = result.get(0);
            TaskDTO dto = wrapper.getData();

            assertThat(dto.getTitle()).isEqualTo("Exam");
            assertThat(dto.getCategory()).isEqualTo(TaskCategory.EXAM);
            assertThat(dto.getWeeklyTimeLoad()).isEqualTo(WEEKLY_LOAD);

            verify(taskRepository).findAllByModuleUserUserId(USER_ID);
        }
    }

    // ------------------------------------------------------------
    // createTask
    // ------------------------------------------------------------
    @Nested
    @DisplayName("createTask")
    class CreateTaskTests {

        @Test
        @DisplayName("wirft ValidationException bei fehlenden Pflichtfeldern")
        void throwsOnMissingRequiredFields() {
            TaskDTO invalid = new OtherTaskDTO(); // bewusst leer

            assertThatThrownBy(() -> sut.createTask(USER_ID, invalid))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("erstellt Task, verknüpft mit Modul und speichert")
        void createsTaskAndLinksToModule() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = new Module(NEW_MODULE_TITLE, anyPriority());
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));

            UUID generatedId = UUID.randomUUID();

            Task savedMock = mock(Task.class);
            when(savedMock.getTaskId()).thenReturn(generatedId);

            when(taskRepository.save(any(Task.class))).thenReturn(savedMock);

            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);

            UUID createdId = sut.createTask(USER_ID, dto);

            assertThat(createdId).isEqualTo(generatedId);

            ArgumentCaptor<Task> savedCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(savedCaptor.capture());

            Task saved = savedCaptor.getValue();
            assertThat(saved).isInstanceOf(OtherTask.class);
            assertThat(saved.getTitle()).isEqualTo(dto.getTitle());
            assertThat(saved.getCategory()).isEqualTo(TaskCategory.OTHER);
            assertThat(saved.getWeeklyDurationMinutes()).isEqualTo(WEEKLY_LOAD);

            assertThat(saved.getModule()).isNotNull();
            assertThat(saved.getModule().getTitle()).isEqualTo(NEW_MODULE_TITLE);
        }

    }

    // ------------------------------------------------------------
    // updateTask
    // ------------------------------------------------------------
    @Nested
    @DisplayName("updateTask")
    class UpdateTaskTests {

        @Test
        @DisplayName("wirft ValidationException, wenn Category geändert werden soll")
        void throwsWhenCategoryWouldChange() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            // bestehende OTHER-Task (Category = OTHER)
            OtherTask existing = new OtherTask("Old", WEEKLY_LOAD, OTHER_START, OTHER_END);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(existing));

            // DTO hat Category EXAM -> soll failen
            ExamTaskDTO dto = validExamDto(OLD_MODULE_ID);

            assertThatThrownBy(() -> sut.updateTask(USER_ID, TASK_ID, dto))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("bei Modulwechsel wird Task aus altem Modul entfernt und in neues Modul übernommen")
        void movesTaskBetweenModulesWhenModuleChanges() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module oldModule = new Module(OLD_MODULE_TITLE, anyPriority());
            Module newModule = new Module(NEW_MODULE_TITLE, anyPriority());

            SubmissionTask existing = new SubmissionTask(
                    "Old Submission",
                    WEEKLY_LOAD,
                    T0.plusDays(1),
                    1,
                    T0.plusWeeks(10)
            );
            oldModule.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID)).thenReturn(Optional.of(newModule));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            SubmissionTaskDTO dto = validSubmissionDto(NEW_MODULE_ID);
            dto.setTitle("Updated Submission");
            dto.setWeeklyTimeLoad(200);

            TaskDTO updated = sut.updateTask(USER_ID, TASK_ID, dto);

            assertThat(updated.getTitle()).isEqualTo("Updated Submission");
            assertThat(updated.getWeeklyTimeLoad()).isEqualTo(200);

            assertThat(oldModule.getTasks()).doesNotContain(existing);
            assertThat(newModule.getTasks()).contains(existing);
            assertThat(existing.getModule()).isSameAs(newModule);

            verify(taskRepository).save(existing);
        }

        @Test
        @DisplayName("wirft AccessDeniedException, wenn Task existiert aber nicht dem User gehört")
        void throwsWhenTaskNotOwned() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            when(taskRepository.existsById(TASK_ID)).thenReturn(true);

            OtherTaskDTO dto = validOtherDto(OLD_MODULE_ID);

            assertThatThrownBy(() -> sut.updateTask(USER_ID, TASK_ID, dto))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------
    // deleteTask
    // ------------------------------------------------------------
    @Nested
    @DisplayName("deleteTask")
    class DeleteTaskTests {

        @Test
        @DisplayName("entfernt Task aus Modul-Collection und löscht über Repository")
        void removesFromModuleAndDeletes() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = new Module(OLD_MODULE_TITLE, anyPriority());
            OtherTask existing = new OtherTask("ToDelete", WEEKLY_LOAD, OTHER_START, OTHER_END);
            module.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(existing));

            assertThat(module.getTasks()).contains(existing);

            sut.deleteTask(USER_ID, TASK_ID);

            assertThat(module.getTasks()).doesNotContain(existing);
            verify(taskRepository).delete(existing);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException, wenn User nicht existiert")
        void throwsWhenUserDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> sut.deleteTask(USER_ID, TASK_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any());
        }
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------

    private static ModulePriority anyPriority() {
        return ModulePriority.values()[0];
    }

    private static OtherTaskDTO validOtherDto(UUID moduleId) {
        OtherTaskDTO dto = new OtherTaskDTO();
        dto.setTitle("Other Title");
        dto.setModuleId(moduleId);
        dto.setCategory(TaskCategory.OTHER);
        dto.setWeeklyTimeLoad(WEEKLY_LOAD);
        dto.setStartTime(OTHER_START);
        dto.setEndTime(OTHER_END);
        return dto;
    }

    private static SubmissionTaskDTO validSubmissionDto(UUID moduleId) {
        SubmissionTaskDTO dto = new SubmissionTaskDTO();
        dto.setTitle("Submission Title");
        dto.setModuleId(moduleId);
        dto.setCategory(TaskCategory.SUBMISSION);
        dto.setWeeklyTimeLoad(WEEKLY_LOAD);

        LocalDateTime first = T0.plusDays(1);
        dto.setFirstDeadline(first);
        dto.setSubmissionCycle(1);
        dto.setEndTime(first.plusWeeks(4));

        return dto;
    }

    private static ExamTaskDTO validExamDto(UUID moduleId) {
        ExamTaskDTO dto = new ExamTaskDTO();
        dto.setTitle("Exam Title");
        dto.setModuleId(moduleId);
        dto.setCategory(TaskCategory.EXAM);
        dto.setWeeklyTimeLoad(WEEKLY_LOAD);
        dto.setExamDate(EXAM_DATE);
        return dto;
    }
}

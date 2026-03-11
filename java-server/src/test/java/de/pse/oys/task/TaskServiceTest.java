package de.pse.oys.task;

import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.OtherTask;
import de.pse.oys.domain.SubmissionTask;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.dto.controller.WrapperDTO;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private static final UUID THIRD_MODULE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID CREATED_TASK_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final String OLD_MODULE_TITLE = "Old Module";
    private static final String NEW_MODULE_TITLE = "New Module";

    private static final int WEEKLY_LOAD = 120;
    private static final int MINUTES_PER_WEEK = 7 * 24 * 60;

    private static final LocalDateTime T0 = LocalDateTime.of(2030, 1, 1, 12, 0);
    private static final LocalDateTime OTHER_START = T0.plusDays(1);
    private static final LocalDateTime OTHER_END = T0.plusDays(2);
    private static final LocalDate EXAM_DATE = LocalDate.of(2030, 1, 15);

    @Mock
    private UserRepository userRepository;
    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private TaskRepository taskRepository;

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
        @DisplayName("wirft NullPointerException bei null userId")
        void throwsWhenUserIdIsNull() {
            assertThatThrownBy(() -> sut.getTasksByUserId(null))
                    .isInstanceOf(NullPointerException.class);

            verifyNoInteractions(userRepository, moduleRepository, taskRepository);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException, wenn User nicht existiert")
        void throwsWhenUserDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> sut.getTasksByUserId(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).findAllByModuleUserUserId(any());
        }

        @Test
        @DisplayName("mappt Exam-, Submission- und Other-Tasks korrekt zu DTOs")
        void returnsMappedDtosForAllCategories() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);

            ExamTask examTask = new ExamTask("Exam", WEEKLY_LOAD, EXAM_DATE);
            ReflectionTestUtils.setField(examTask, "taskId", UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            module.addTask(examTask);

            SubmissionTask submissionTask = new SubmissionTask(
                    "Submission",
                    180,
                    T0.plusDays(3),
                    2,
                    T0.plusWeeks(3)
            );
            ReflectionTestUtils.setField(submissionTask, "taskId", UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
            module.addTask(submissionTask);

            OtherTask otherTask = new OtherTask("Other", 90, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(otherTask, "taskId", UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
            module.addTask(otherTask);

            when(taskRepository.findAllByModuleUserUserId(USER_ID))
                    .thenReturn(List.of(examTask, submissionTask, otherTask));

            List<WrapperDTO<TaskDTO>> result = sut.getTasksByUserId(USER_ID);

            assertThat(result).hasSize(3);

            TaskDTO examDto = result.get(0).getData();
            assertThat(examDto).isInstanceOf(ExamTaskDTO.class);
            assertThat(examDto.getTitle()).isEqualTo("Exam");
            assertThat(examDto.getCategory()).isEqualTo(TaskCategory.EXAM);
            assertThat(examDto.getWeeklyTimeLoad()).isEqualTo(WEEKLY_LOAD);
            assertThat(examDto.getModuleId()).isEqualTo(OLD_MODULE_ID);
            assertThat(((ExamTaskDTO) examDto).getExamDate()).isEqualTo(EXAM_DATE);

            TaskDTO submissionDto = result.get(1).getData();
            assertThat(submissionDto).isInstanceOf(SubmissionTaskDTO.class);
            assertThat(submissionDto.getTitle()).isEqualTo("Submission");
            assertThat(submissionDto.getCategory()).isEqualTo(TaskCategory.SUBMISSION);
            assertThat(submissionDto.getModuleId()).isEqualTo(OLD_MODULE_ID);
            assertThat(((SubmissionTaskDTO) submissionDto).getFirstDeadline()).isEqualTo(T0.plusDays(3));
            assertThat(((SubmissionTaskDTO) submissionDto).getSubmissionCycle()).isEqualTo(2);
            assertThat(((SubmissionTaskDTO) submissionDto).getEndTime()).isEqualTo(T0.plusWeeks(3));

            TaskDTO otherDto = result.get(2).getData();
            assertThat(otherDto).isInstanceOf(OtherTaskDTO.class);
            assertThat(otherDto.getTitle()).isEqualTo("Other");
            assertThat(otherDto.getCategory()).isEqualTo(TaskCategory.OTHER);
            assertThat(otherDto.getModuleId()).isEqualTo(OLD_MODULE_ID);
            assertThat(((OtherTaskDTO) otherDto).getStartTime()).isEqualTo(OTHER_START);
            assertThat(((OtherTaskDTO) otherDto).getEndTime()).isEqualTo(OTHER_END);
        }

        @Test
        @DisplayName("setzt moduleId im DTO auf null, wenn Task kein Modul hat")
        void returnsDtoWithNullModuleIdWhenTaskHasNoModule() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            OtherTask otherTask = new OtherTask("Orphan", 50, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(otherTask, "taskId", UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));

            when(taskRepository.findAllByModuleUserUserId(USER_ID)).thenReturn(List.of(otherTask));

            List<WrapperDTO<TaskDTO>> result = sut.getTasksByUserId(USER_ID);

            assertThat(result).hasSize(1);
            TaskDTO dto = result.get(0).getData();
            assertThat(dto.getModuleId()).isNull();
            assertThat(dto.getCategory()).isEqualTo(TaskCategory.OTHER);
        }
    }

    // ------------------------------------------------------------
    // createTask
    // ------------------------------------------------------------
    @Nested
    @DisplayName("createTask")
    class CreateTaskTests {

        @Test
        @DisplayName("wirft NullPointerException bei null userId")
        void throwsOnNullUserId() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);

            assertThatThrownBy(() -> sut.createTask(null, dto))
                    .isInstanceOf(NullPointerException.class);

            verifyNoInteractions(userRepository, moduleRepository, taskRepository);
        }

        @Test
        @DisplayName("wirft ValidationException bei null DTO")
        void throwsOnNullDto() {
            assertThatThrownBy(() -> sut.createTask(USER_ID, null))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("wirft ValidationException bei fehlenden Pflichtfeldern")
        void throwsOnMissingRequiredFields() {
            TaskDTO invalid = new OtherTaskDTO();

            assertThatThrownBy(() -> sut.createTask(USER_ID, invalid))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("wirft ValidationException bei weeklyTimeLoad <= 0")
        void throwsOnWeeklyLoadTooSmall() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
            dto.setWeeklyTimeLoad(0);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("wirft ValidationException bei weeklyTimeLoad > Minuten einer Woche")
        void throwsOnWeeklyLoadTooLarge() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
            dto.setWeeklyTimeLoad(MINUTES_PER_WEEK + 1);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("wirft ValidationException wenn Exam-Date fehlt")
        void throwsWhenExamDateMissing() {
            ExamTaskDTO dto = validExamDto(NEW_MODULE_ID);
            dto.setExamDate(null);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("wirft ValidationException wenn Submission-Pflichtfelder fehlen")
        void throwsWhenSubmissionFieldsMissing() {
            SubmissionTaskDTO dto = validSubmissionDto();
            dto.setFirstDeadline(null);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("wirft ValidationException wenn submissionCycle < 1")
        void throwsWhenSubmissionCycleInvalid() {
            SubmissionTaskDTO dto = validSubmissionDto();
            dto.setSubmissionCycle(0);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("wirft ValidationException wenn Submission-Ende nicht nach FirstDeadline liegt")
        void throwsWhenSubmissionRangeInvalid() {
            SubmissionTaskDTO dto = validSubmissionDto();
            dto.setEndTime(dto.getFirstDeadline());

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("wirft ValidationException wenn Other-EndTime fehlt")
        void throwsWhenOtherEndTimeMissing() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
            dto.setEndTime(null);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("wirft ValidationException wenn Other-Pflichtfelder fehlen")
        void throwsWhenOtherFieldsMissing() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
            dto.setStartTime(null);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("wirft ValidationException wenn Other-Ende nicht nach Start liegt")
        void throwsWhenOtherRangeInvalid() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
            dto.setEndTime(dto.getStartTime());

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException wenn User nicht existiert")
        void throwsWhenUserDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(moduleRepository, never()).findByModuleIdAndUser_UserId(any(), any());
            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException wenn Modul nicht existiert oder nicht dem User gehört")
        void throwsWhenModuleNotFound() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID))
                    .thenReturn(Optional.empty());

            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);

            assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("erstellt OtherTask, verknüpft mit Modul und speichert")
        void createsOtherTaskAndLinksToModule() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = moduleWithId(NEW_MODULE_ID, NEW_MODULE_TITLE);
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));

            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                ReflectionTestUtils.setField(task, "taskId", CREATED_TASK_ID);
                return task;
            });

            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);

            UUID createdId = sut.createTask(USER_ID, dto);

            assertThat(createdId).isEqualTo(CREATED_TASK_ID);

            ArgumentCaptor<Task> savedCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(savedCaptor.capture());

            Task saved = savedCaptor.getValue();
            assertThat(saved).isInstanceOf(OtherTask.class);
            assertThat(saved.getTitle()).isEqualTo(dto.getTitle());
            assertThat(saved.getCategory()).isEqualTo(TaskCategory.OTHER);
            assertThat(saved.getWeeklyDurationMinutes()).isEqualTo(WEEKLY_LOAD);
            assertThat(saved.getModule()).isSameAs(module);
            assertThat(module.getTasks()).contains(saved);
        }

        @Test
        @DisplayName("erstellt SubmissionTask korrekt")
        void createsSubmissionTask() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = moduleWithId(NEW_MODULE_ID, NEW_MODULE_TITLE);
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));

            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                ReflectionTestUtils.setField(task, "taskId", CREATED_TASK_ID);
                return task;
            });

            SubmissionTaskDTO dto = validSubmissionDto();

            UUID createdId = sut.createTask(USER_ID, dto);

            assertThat(createdId).isEqualTo(CREATED_TASK_ID);

            ArgumentCaptor<Task> savedCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(savedCaptor.capture());

            assertThat(savedCaptor.getValue()).isInstanceOf(SubmissionTask.class);
            SubmissionTask saved = (SubmissionTask) savedCaptor.getValue();
            assertThat(saved.getFirstDeadline()).isEqualTo(dto.getFirstDeadline());
            assertThat(saved.getCycleWeeks()).isEqualTo(dto.getSubmissionCycle());
            assertThat(saved.getEndTime()).isEqualTo(dto.getEndTime());
        }

        @Test
        @DisplayName("erstellt ExamTask korrekt")
        void createsExamTask() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = moduleWithId(NEW_MODULE_ID, NEW_MODULE_TITLE);
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));

            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                ReflectionTestUtils.setField(task, "taskId", CREATED_TASK_ID);
                return task;
            });

            ExamTaskDTO dto = validExamDto(NEW_MODULE_ID);

            UUID createdId = sut.createTask(USER_ID, dto);

            assertThat(createdId).isEqualTo(CREATED_TASK_ID);

            ArgumentCaptor<Task> savedCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(savedCaptor.capture());

            assertThat(savedCaptor.getValue()).isInstanceOf(ExamTask.class);
            ExamTask saved = (ExamTask) savedCaptor.getValue();
            assertThat(saved.getExamDate()).isEqualTo(dto.getExamDate());
        }
    }

    @Test
    @DisplayName("wirft ValidationException bei leerem Title")
    void throwsOnBlankTitle() {
        OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
        dto.setTitle("   ");

        assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                .isInstanceOf(ValidationException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("wirft ValidationException bei null moduleId")
    void throwsOnNullModuleId() {
        OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
        dto.setModuleId(null);

        assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                .isInstanceOf(ValidationException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("wirft ValidationException bei null category")
    void throwsOnNullCategory() {
        OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
        dto.setCategory(null);

        assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                .isInstanceOf(ValidationException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("wirft ValidationException bei null weeklyTimeLoad")
    void throwsOnNullWeeklyLoad() {
        OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
        dto.setWeeklyTimeLoad(null);

        assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                .isInstanceOf(ValidationException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("wirft ValidationException wenn Submission-EndTime fehlt")
    void throwsWhenSubmissionEndTimeMissing() {
        SubmissionTaskDTO dto = validSubmissionDto();
        dto.setEndTime(null);

        assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                .isInstanceOf(ValidationException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("wirft ValidationException wenn Submission-Cycle fehlt")
    void throwsWhenSubmissionCycleMissing() {
        SubmissionTaskDTO dto = validSubmissionDto();
        dto.setSubmissionCycle(null);

        assertThatThrownBy(() -> sut.createTask(USER_ID, dto))
                .isInstanceOf(ValidationException.class);

        verify(taskRepository, never()).save(any());
    }

    // ------------------------------------------------------------
    // updateTask
    // ------------------------------------------------------------
    @Nested
    @DisplayName("updateTask")
    class UpdateTaskTests {

        @Test
        @DisplayName("wirft NullPointerException bei null taskId")
        void throwsOnNullTaskId() {
            OtherTaskDTO dto = validOtherDto(OLD_MODULE_ID);

            assertThatThrownBy(() -> sut.updateTask(USER_ID, null, dto))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException wenn Task nicht existiert")
        void throwsWhenTaskDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());
            when(taskRepository.existsById(TASK_ID)).thenReturn(false);

            OtherTaskDTO dto = validOtherDto(OLD_MODULE_ID);

            assertThatThrownBy(() -> sut.updateTask(USER_ID, TASK_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).save(any());
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

        @Test
        @DisplayName("bei Kategoriewechsel wird der alte Task gelöscht und ein neuer erstellt")
        void changesCategoryByRecreatingTask() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            OtherTask existing = new OtherTask("Old", WEEKLY_LOAD, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);
            Module module = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);
            module.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(OLD_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));

            ExamTaskDTO newDto = validExamDto(OLD_MODULE_ID);

            ExamTask savedNewTask = new ExamTask(newDto.getTitle(), WEEKLY_LOAD, EXAM_DATE);
            ReflectionTestUtils.setField(savedNewTask, "taskId", CREATED_TASK_ID);

            when(taskRepository.save(any(Task.class))).thenReturn(savedNewTask);
            when(taskRepository.findById(CREATED_TASK_ID)).thenReturn(Optional.of(savedNewTask));

            UUID resultId = sut.updateTask(USER_ID, TASK_ID, newDto);

            verify(taskRepository).delete(existing);
            verify(taskRepository, atLeastOnce()).save(any(ExamTask.class));
            assertThat(resultId).isEqualTo(CREATED_TASK_ID);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException wenn neu erstellter Task nach Kategoriewechsel nicht gefunden wird")
        void throwsWhenRecreatedTaskCannotBeLoaded() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            OtherTask existing = new OtherTask("Old", WEEKLY_LOAD, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);
            Module module = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);
            module.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(OLD_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));

            ExamTaskDTO newDto = validExamDto(OLD_MODULE_ID);

            ExamTask savedNewTask = new ExamTask(newDto.getTitle(), WEEKLY_LOAD, EXAM_DATE);
            ReflectionTestUtils.setField(savedNewTask, "taskId", CREATED_TASK_ID);

            when(taskRepository.save(any(Task.class))).thenReturn(savedNewTask);
            when(taskRepository.findById(CREATED_TASK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.updateTask(USER_ID, TASK_ID, newDto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository).delete(existing);
        }

        @Test
        @DisplayName("aktualisiert ExamTask im gleichen Modul")
        void updatesExamTaskInSameModule() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);
            ExamTask existing = new ExamTask("Old Exam", WEEKLY_LOAD, EXAM_DATE.minusDays(1));
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);
            module.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(OLD_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(module));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ExamTaskDTO dto = validExamDto(OLD_MODULE_ID);
            dto.setTitle("Updated Exam");
            dto.setWeeklyTimeLoad(240);
            dto.setExamDate(EXAM_DATE.plusDays(5));

            UUID resultId = sut.updateTask(USER_ID, TASK_ID, dto);

            assertThat(resultId).isEqualTo(TASK_ID);
            assertThat(existing.getTitle()).isEqualTo("Updated Exam");
            assertThat(existing.getWeeklyDurationMinutes()).isEqualTo(240);
            assertThat(existing.getExamDate()).isEqualTo(EXAM_DATE.plusDays(5));
            assertThat(existing.getModule()).isSameAs(module);

            verify(taskRepository).save(existing);
            verify(taskRepository, never()).delete(existing);
        }

        @Test
        @DisplayName("bei Modulwechsel wird SubmissionTask in neues Modul übernommen")
        void movesTaskBetweenModulesWhenModuleChanges() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module oldModule = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);
            Module newModule = moduleWithId(NEW_MODULE_ID, NEW_MODULE_TITLE);

            SubmissionTask existing = new SubmissionTask(
                    "Old Submission",
                    WEEKLY_LOAD,
                    T0.plusDays(1),
                    1,
                    T0.plusWeeks(10)
            );
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);
            oldModule.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID)).thenReturn(Optional.of(newModule));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            SubmissionTaskDTO dto = validSubmissionDto();
            dto.setTitle("Updated Submission");
            dto.setWeeklyTimeLoad(200);

            UUID resultId = sut.updateTask(USER_ID, TASK_ID, dto);

            assertThat(resultId).isEqualTo(TASK_ID);
            assertThat(existing.getTitle()).isEqualTo("Updated Submission");
            assertThat(existing.getWeeklyDurationMinutes()).isEqualTo(200);
            assertThat(existing.getFirstDeadline()).isEqualTo(dto.getFirstDeadline());
            assertThat(existing.getCycleWeeks()).isEqualTo(dto.getSubmissionCycle());
            assertThat(existing.getEndTime()).isEqualTo(dto.getEndTime());

            assertThat(newModule.getTasks()).contains(existing);
            assertThat(existing.getModule()).isSameAs(newModule);

            verify(taskRepository).save(existing);
        }

        @Test
        @DisplayName("aktualisiert OtherTask auch ohne Modulzuordnung")
        void updatesOtherTaskWhenOldModuleIsNull() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            OtherTask existing = new OtherTask("Old Other", WEEKLY_LOAD, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);

            Module targetModule = moduleWithId(THIRD_MODULE_ID, "Third Module");

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(THIRD_MODULE_ID, USER_ID))
                    .thenReturn(Optional.of(targetModule));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            OtherTaskDTO dto = validOtherDto(THIRD_MODULE_ID);
            dto.setTitle("Updated Other");
            dto.setWeeklyTimeLoad(300);
            dto.setStartTime(OTHER_START.plusHours(1));
            dto.setEndTime(OTHER_END.plusHours(2));

            UUID resultId = sut.updateTask(USER_ID, TASK_ID, dto);

            assertThat(resultId).isEqualTo(TASK_ID);
            assertThat(existing.getTitle()).isEqualTo("Updated Other");
            assertThat(existing.getWeeklyDurationMinutes()).isEqualTo(300);
            assertThat(existing.getStartTime()).isEqualTo(OTHER_START.plusHours(1));
            assertThat(existing.getHardDeadline()).isEqualTo(OTHER_END.plusHours(2));
            assertThat(existing.getModule()).isNull();

            verify(taskRepository).save(existing);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException wenn Zielmodul beim normalen Update nicht gefunden wird")
        void throwsWhenTargetModuleNotFoundDuringNormalUpdate() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            ExamTask existing = new ExamTask("Old Exam", WEEKLY_LOAD, EXAM_DATE);
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);
            Module oldModule = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);
            oldModule.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID))
                    .thenReturn(Optional.of(existing));
            when(moduleRepository.findByModuleIdAndUser_UserId(NEW_MODULE_ID, USER_ID))
                    .thenReturn(Optional.empty());

            ExamTaskDTO dto = validExamDto(NEW_MODULE_ID);

            assertThatThrownBy(() -> sut.updateTask(USER_ID, TASK_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

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
        @DisplayName("wirft NullPointerException bei null userId")
        void throwsWhenUserIdIsNull() {
            assertThatThrownBy(() -> sut.deleteTask(null, TASK_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("wirft NullPointerException bei null taskId")
        void throwsWhenTaskIdIsNull() {
            assertThatThrownBy(() -> sut.deleteTask(USER_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException, wenn User nicht existiert")
        void throwsWhenUserDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> sut.deleteTask(USER_ID, TASK_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any());
        }

        @Test
        @DisplayName("wirft ResourceNotFoundException, wenn Task nicht existiert")
        void throwsWhenTaskDoesNotExist() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());
            when(taskRepository.existsById(TASK_ID)).thenReturn(false);

            assertThatThrownBy(() -> sut.deleteTask(USER_ID, TASK_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any());
        }

        @Test
        @DisplayName("wirft AccessDeniedException, wenn Task existiert aber nicht dem User gehört")
        void throwsWhenTaskNotOwned() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());
            when(taskRepository.existsById(TASK_ID)).thenReturn(true);

            assertThatThrownBy(() -> sut.deleteTask(USER_ID, TASK_ID))
                    .isInstanceOf(AccessDeniedException.class);

            verify(taskRepository, never()).delete(any());
        }

        @Test
        @DisplayName("entfernt Task aus Modul-Collection und löscht über Repository")
        void removesFromModuleAndDeletes() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            Module module = moduleWithId(OLD_MODULE_ID, OLD_MODULE_TITLE);
            OtherTask existing = new OtherTask("ToDelete", WEEKLY_LOAD, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);
            module.addTask(existing);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(existing));

            assertThat(module.getTasks()).contains(existing);

            sut.deleteTask(USER_ID, TASK_ID);

            assertThat(module.getTasks()).doesNotContain(existing);
            verify(taskRepository).delete(existing);
        }

        @Test
        @DisplayName("löscht Task auch ohne Modulzuordnung")
        void deletesTaskWithoutModule() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            OtherTask existing = new OtherTask("ToDelete", WEEKLY_LOAD, OTHER_START, OTHER_END);
            ReflectionTestUtils.setField(existing, "taskId", TASK_ID);

            when(taskRepository.findByTaskIdAndModuleUserUserId(TASK_ID, USER_ID))
                    .thenReturn(Optional.of(existing));

            sut.deleteTask(USER_ID, TASK_ID);

            verify(taskRepository).delete(existing);
        }
    }

    // ------------------------------------------------------------
    // private helper coverage
    // ------------------------------------------------------------
    @Nested
    @DisplayName("private helper coverage")
    class PrivateHelperCoverageTests {

        @Test
        @DisplayName("mapToEntity wirft NullPointerException bei null category")
        void mapToEntityThrowsWhenCategoryIsNull() {
            OtherTaskDTO dto = validOtherDto(NEW_MODULE_ID);
            dto.setCategory(null);

            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(sut, "mapToEntity", dto))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------

    private static ModulePriority anyPriority() {
        return ModulePriority.values()[0];
    }

    private static Module moduleWithId(UUID id, String title) {
        Module module = new Module(title, anyPriority());
        ReflectionTestUtils.setField(module, "moduleId", id);
        return module;
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

    private static SubmissionTaskDTO validSubmissionDto() {
        SubmissionTaskDTO dto = new SubmissionTaskDTO();
        dto.setTitle("Submission Title");
        dto.setModuleId(TaskServiceTest.NEW_MODULE_ID);
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
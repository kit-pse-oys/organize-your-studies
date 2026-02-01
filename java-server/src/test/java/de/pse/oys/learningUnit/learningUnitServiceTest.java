package de.pse.oys.service;

import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningUnitServiceTest {

    private static final UUID USER_ID  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PLAN_ID  = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID UNIT_ID  = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private LearningPlanRepository learningPlanRepository;

    @InjectMocks
    private LearningUnitService sut;

    @Test
    void updateLearningUnit_updatesWindow_andReturnsUpdatedPlanDto() {
        LearningPlan plan = new LearningPlan(LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 8));
        setField(plan, "planId", PLAN_ID);

        LearningUnit unit = unitWithTaskAndModule(
                "Task Title",
                "Module Desc",
                "#FF00FF",
                LocalDateTime.of(2026, 2, 2, 10, 0),
                LocalDateTime.of(2026, 2, 2, 11, 0)
        );
        setField(unit, "unitId", UNIT_ID);
        plan.getUnits().add(unit);

        when(learningPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.of(plan));

        UnitDTO dto = new UnitDTO();
        dto.setDate(LocalDate.of(2026, 2, 3));
        dto.setStart(LocalTime.of(14, 0));
        dto.setEnd(LocalTime.of(15, 30));

        LearningPlanDTO result = sut.updateLearningUnit(USER_ID, PLAN_ID, UNIT_ID, dto);

        assertThat(unit.getStartTime()).isEqualTo(LocalDateTime.of(2026, 2, 3, 14, 0));
        assertThat(unit.getEndTime()).isEqualTo(LocalDateTime.of(2026, 2, 3, 15, 30));

        assertThat(result.getId()).isEqualTo(PLAN_ID);
        assertThat(result.getValidFrom()).isEqualTo(plan.getWeekStart());
        assertThat(result.getValidUntil()).isEqualTo(plan.getWeekEnd());

        assertThat(result.getUnits()).hasSize(1);
        UnitDTO mapped = result.getUnits().get(0);
        assertThat(mapped.getTitle()).isEqualTo("Task Title");
        assertThat(mapped.getDescription()).isEqualTo("Module Desc");
        assertThat(mapped.getColor()).isEqualTo("#FF00FF");
        assertThat(mapped.getDate()).isEqualTo(LocalDate.of(2026, 2, 3));
        assertThat(mapped.getStart()).isEqualTo(LocalTime.of(14, 0));
        assertThat(mapped.getEnd()).isEqualTo(LocalTime.of(15, 30));

        verify(learningPlanRepository).findByIdAndUserId(PLAN_ID, USER_ID);
        verify(learningPlanRepository).save(plan);
        verifyNoMoreInteractions(learningPlanRepository);
    }

    @Test
    void updateLearningUnit_missingTimeFields_throwsValidation_andDoesNotHitRepository() {
        UnitDTO dto = new UnitDTO();
        dto.setDate(LocalDate.of(2026, 2, 3));
        dto.setStart(LocalTime.of(14, 0));
        // end missing

        assertThatThrownBy(() -> sut.updateLearningUnit(USER_ID, PLAN_ID, UNIT_ID, dto))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    void moveLearningUnitManually_overlap_throwsValidation_andDoesNotSave() {
        LearningPlan plan = new LearningPlan(LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 8));
        setField(plan, "planId", PLAN_ID);

        LearningUnit target = unitWithTaskAndModule(
                "T1", null, null,
                LocalDateTime.of(2026, 2, 2, 10, 0),
                LocalDateTime.of(2026, 2, 2, 11, 0)
        );
        setField(target, "unitId", UNIT_ID);

        LearningUnit other = unitWithTaskAndModule(
                "T2", null, null,
                LocalDateTime.of(2026, 2, 2, 12, 0),
                LocalDateTime.of(2026, 2, 2, 13, 0)
        );
        setField(other, "unitId", OTHER_ID);

        plan.getUnits().add(target);
        plan.getUnits().add(other);

        when(learningPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.of(plan));

        LocalDateTime newStart = LocalDateTime.of(2026, 2, 2, 12, 30);
        LocalDateTime newEnd = LocalDateTime.of(2026, 2, 2, 13, 30);

        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, PLAN_ID, UNIT_ID, newStart, newEnd))
                .isInstanceOf(ValidationException.class);

        verify(learningPlanRepository).findByIdAndUserId(PLAN_ID, USER_ID);
        verify(learningPlanRepository, never()).save(any());
        verifyNoMoreInteractions(learningPlanRepository);
    }

    @Test
    void finishUnitEarly_negativeDuration_throwsValidation_andDoesNotHitRepository() {
        assertThatThrownBy(() -> sut.finishUnitEarly(USER_ID, PLAN_ID, UNIT_ID, -1))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    void loadPlan_userScopeMiss_throwsAccessDenied() {
        when(learningPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.empty());

        UnitDTO dto = new UnitDTO();
        dto.setDate(LocalDate.of(2026, 2, 3));
        dto.setStart(LocalTime.of(14, 0));
        dto.setEnd(LocalTime.of(15, 0));

        assertThatThrownBy(() -> sut.updateLearningUnit(USER_ID, PLAN_ID, UNIT_ID, dto))
                .isInstanceOf(AccessDeniedException.class);

        verify(learningPlanRepository).findByIdAndUserId(PLAN_ID, USER_ID);
        verifyNoMoreInteractions(learningPlanRepository);
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private static LearningUnit unitWithTaskAndModule(String taskTitle, String moduleDesc, String moduleColor,
                                                      LocalDateTime start, LocalDateTime end) {
        Module module = new Module("Module", ModulePriority.MEDIUM);
        module.setDescription(moduleDesc);
        module.setColorHexCode(moduleColor);

        Task task = new TestTask(taskTitle);
        task.setModule(module);

        return new LearningUnit(task, start, end);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to set field '" + fieldName + "'", e);
        }
    }

    private static final class TestTask extends Task { //TODO

        private TestTask(String title) {
            // Wenn eure Task-Basisklasse so aussieht:
            // Task(String title, int weeklyDurationMinutes, TaskCategory category)
            // dann passt das:
            super(title, 1, TaskCategory.OTHER);
        }

        @Override
        public LocalDateTime getHardDeadline() {
            return null; // für den Test egal
        }

        @Override
        protected boolean isActive() {
            return true; // Test-Task immer aktiv
        }
    }


}

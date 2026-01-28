package de.pse.oys.learningUnit;

import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Task;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.service.LearningUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den LearningUnitService.
 *
 * @author uqvfm
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class LearningUnitServiceTest {

    @Mock private LearningPlanRepository learningPlanRepository;
    @Mock private TaskRepository taskRepository;

    private LearningUnitService service;

    private UUID userId;
    private UUID planId;
    private UUID unitId;

    private LearningPlan plan;
    private LearningUnit unit;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();
        unitId = UUID.randomUUID();

        service = new LearningUnitService(learningPlanRepository, taskRepository);

        plan = mock(LearningPlan.class);
        unit = mock(LearningUnit.class);

        lenient().when(plan.getPlanId()).thenReturn(planId);
        lenient().when(unit.getUnitId()).thenReturn(unitId);

        List<LearningUnit> units = new ArrayList<>();
        units.add(unit);
        lenient().when(plan.getUnits()).thenReturn(units);

        lenient().when(learningPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        lenient().when(learningPlanRepository.save(any(LearningPlan.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        lenient().when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // updateLearningUnit(...)
    // -------------------------------------------------------------------------

    @Test
    void updateLearningUnit_updatesTimes_andReturnsUpdatedPlanDto() {
        UnitDTO dto = mock(UnitDTO.class);

        LocalDate date = LocalDate.of(2026, 1, 27);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 30);

        when(dto.getDate()).thenReturn(date);
        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);

        LearningPlanDTO result = service.updateLearningUnit(userId, planId, unitId, dto);

        assertNotNull(result);

        verify(learningPlanRepository).findById(planId);

        verify(unit).setStartTime(LocalDateTime.of(date, start));
        verify(unit).setEndTime(LocalDateTime.of(date, end));

        verify(learningPlanRepository, atLeastOnce()).save(any(LearningPlan.class));
    }

    @Test
    void updateLearningUnit_updatesTaskTitle_whenPresent() {
        UnitDTO dto = mock(UnitDTO.class);
        when(dto.getTitle()).thenReturn("Neuer Titel");

        Task task = mock(Task.class);
        when(unit.getTask()).thenReturn(task);

        LearningPlanDTO result = service.updateLearningUnit(userId, planId, unitId, dto);

        assertNotNull(result);

        verify(task).setTitle("Neuer Titel");
        verify(taskRepository, atLeastOnce()).save(task);
        verify(learningPlanRepository, atLeastOnce()).save(any(LearningPlan.class));
    }

    @Test
    void updateLearningUnit_doesNotSaveTask_whenTaskNull() {
        UnitDTO dto = mock(UnitDTO.class);
        when(dto.getTitle()).thenReturn("Neuer Titel");
        when(unit.getTask()).thenReturn(null);

        LearningPlanDTO result = service.updateLearningUnit(userId, planId, unitId, dto);

        assertNotNull(result);

        verifyNoInteractions(taskRepository);
        verify(learningPlanRepository, atLeastOnce()).save(any(LearningPlan.class));
    }

    @Test
    void updateLearningUnit_throwsIfDtoNull() {
        assertThrows(NullPointerException.class,
                () -> service.updateLearningUnit(userId, planId, unitId, null));

        verify(learningPlanRepository, never()).save(any());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateLearningUnit_throwsIfPlanNotFound() {
        when(learningPlanRepository.findById(planId)).thenReturn(Optional.empty());

        UnitDTO dto = mock(UnitDTO.class);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateLearningUnit(userId, planId, unitId, dto));

        verify(learningPlanRepository).findById(planId);
        verify(learningPlanRepository, never()).save(any());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateLearningUnit_throwsIfUnitNotInPlan() {
        lenient().when(plan.getUnits()).thenReturn(List.of()); // überschreiben, aber lenient

        UnitDTO dto = mock(UnitDTO.class);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateLearningUnit(userId, planId, unitId, dto));

        verify(learningPlanRepository).findById(planId);
        verify(learningPlanRepository, never()).save(any());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateLearningUnit_throwsIfEndNotAfterStart() {
        UnitDTO dto = mock(UnitDTO.class);

        LocalDate date = LocalDate.of(2026, 1, 27);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(10, 0); // ungültig

        when(dto.getDate()).thenReturn(date);
        when(dto.getStart()).thenReturn(start);
        when(dto.getEnd()).thenReturn(end);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateLearningUnit(userId, planId, unitId, dto));

        verify(learningPlanRepository, never()).save(any());
        verify(unit, never()).setStartTime(any());
        verify(unit, never()).setEndTime(any());
        verifyNoInteractions(taskRepository);
    }

    // -------------------------------------------------------------------------
    // moveLearningUnitManually(...)
    // -------------------------------------------------------------------------

    @Test
    void moveLearningUnitManually_updatesTimes_andReturnsUpdatedPlanDto() {
        LocalDateTime newStart = LocalDateTime.of(2026, 1, 27, 14, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 1, 27, 15, 0);

        LearningPlanDTO result = service.moveLearningUnitManually(userId, planId, unitId, newStart, newEnd);

        assertNotNull(result);

        verify(learningPlanRepository).findById(planId);

        verify(unit).setStartTime(newStart);
        verify(unit).setEndTime(newEnd);

        verify(learningPlanRepository, atLeastOnce()).save(any(LearningPlan.class));
    }

    @Test
    void moveLearningUnitManually_throwsIfEndNotAfterStart() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 27, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 27, 14, 0);

        assertThrows(IllegalArgumentException.class,
                () -> service.moveLearningUnitManually(userId, planId, unitId, start, end));

        verify(learningPlanRepository, never()).save(any());
        verify(unit, never()).setStartTime(any());
        verify(unit, never()).setEndTime(any());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void moveLearningUnitManually_throwsIfUnitNotInPlan() {
        lenient().when(plan.getUnits()).thenReturn(List.of()); // lenient!

        assertThrows(IllegalArgumentException.class,
                () -> service.moveLearningUnitManually(
                        userId, planId, unitId,
                        LocalDateTime.now(), LocalDateTime.now().plusMinutes(30)));

        verify(learningPlanRepository).findById(planId);
        verify(learningPlanRepository, never()).save(any());
        verifyNoInteractions(taskRepository);
    }

    // -------------------------------------------------------------------------
    // finishUnitEarly(...)
    // -------------------------------------------------------------------------

    @Test
    void finishUnitEarly_marksCompletedEarly_andReturnsUpdatedPlanDto() {
        int actualDuration = 42;

        LearningPlanDTO result = service.finishUnitEarly(userId, planId, unitId, actualDuration);

        assertNotNull(result);

        verify(learningPlanRepository).findById(planId);
        verify(unit).markAsCompletedEarly(actualDuration);
        verify(learningPlanRepository, atLeastOnce()).save(any(LearningPlan.class));
    }

    @Test
    void finishUnitEarly_throwsIfActualDurationNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> service.finishUnitEarly(userId, planId, unitId, -1));

        verify(learningPlanRepository, never()).save(any());
        verify(unit, never()).markAsCompletedEarly(anyInt());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void finishUnitEarly_whenActualDurationNull_throwsIfUnitStartTimeNull() {
        when(unit.getStartTime()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.finishUnitEarly(userId, planId, unitId, null));

        verify(learningPlanRepository, never()).save(any());
        verify(unit, never()).markAsCompletedEarly(anyInt());
        verifyNoInteractions(taskRepository);
    }

    @Test
    void finishUnitEarly_whenActualDurationNull_usesNowDiffAndSaves() {
        when(unit.getStartTime()).thenReturn(LocalDateTime.now().minusMinutes(5));

        LearningPlanDTO result = service.finishUnitEarly(userId, planId, unitId, null);

        assertNotNull(result);

        verify(unit).markAsCompletedEarly(anyInt());
        verify(learningPlanRepository, atLeastOnce()).save(any(LearningPlan.class));
    }
}


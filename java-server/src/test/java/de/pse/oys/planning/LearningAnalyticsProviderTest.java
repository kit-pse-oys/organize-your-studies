package de.pse.oys.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.*;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.dto.CostDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.service.planning.LearningAnalyticsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LearningAnalyticsProviderTest {

    @Mock
    private CostMatrixRepository costMatrixRepository;

    @Mock
    private TaskRepository taskRepository;

    // Wir nutzen einen echten ObjectMapper, da das Mocken von JSON-Parsing oft fehleranfällig ist
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LearningAnalyticsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new LearningAnalyticsProvider(costMatrixRepository, objectMapper, taskRepository);
    }

    /*** --- TEST 1: Task ist null -> Exception ---
     */

    @Test
    void getCostMatrixForTask_TaskNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            provider.getCostMatrixForTask(null);
        });
        assertEquals("Error: Task darf nicht null sein", exception.getMessage());
    }

    /*** --- TEST 2: Matrix aktuell -> Parsed JSON zurückgeben ---
     */

    @Test
    void getCostMatrixForTask_MatrixUpToDate_ReturnsParsedJson() {

        Task task = mock(Task.class);
        CostMatrix matrix = mock(CostMatrix.class);
        String json = "[{\"t\":10, \"c\":-5}]";

        when(task.getCostMatrix()).thenReturn(matrix);
        when(matrix.isOutdated()).thenReturn(false);
        when(matrix.getCosts()).thenReturn(json);

        // Execute
        List<CostDTO> result = provider.getCostMatrixForTask(task);


        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getT());
        assertEquals(-5, result.get(0).getC());
        verify(costMatrixRepository, never()).save(any());
    }

    /*** --- TEST 3: Matrix veraltet -> Berechnung aus Ratings und Persistenz ---
     */
    @Test
    void getCostMatrixForTask_MatrixOutdated_CalculatesFromRatingsAndPersists() {

        Task task = mock(Task.class);
        CostMatrix matrix = new CostMatrix();
        matrix.markAsOutdated();

        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);


        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);


        when(task.getCostMatrix()).thenReturn(matrix);
        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(start);
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.HIGH);
        when(rating.getAchievement()).thenReturn(AchievementLevel.GOOD);

        List<CostDTO> result = provider.getCostMatrixForTask(task);


        assertEquals(1, result.size());
        assertEquals(120, result.get(0).getT());
        assertEquals(-2, result.get(0).getC());


        verify(costMatrixRepository).save(matrix);

        assertNotNull(matrix.getCosts());
        assertTrue(matrix.getCosts().contains("\"c\":-2"));
    }


    /**
     * Da die Methode 'calculateHeuristiksFromTask' privat ist und aktuell im Code
     * (getCostMatrixForTask) NICHT aufgerufen wird, müssen wir Reflection nutzen,
     * um zu testen, ob die Logik prinzipiell stimmt.
     */
    @Test
    void calculateHeuristiksFromTask_FindsSimilarTask() {
        // 1. Setup Current Task
        Task currentTask = mock(Task.class);
        UUID currentId = UUID.randomUUID();
        when(currentTask.getTaskId()).thenReturn(currentId);
        when(currentTask.getCategory()).thenReturn(TaskCategory.EXAM);


        Module module = mock(Module.class);
        when(currentTask.getModule()).thenReturn(module);

        Task otherTask = mock(Task.class);
        UUID otherId = UUID.randomUUID();
        CostMatrix otherMatrix = mock(CostMatrix.class);


        when(otherTask.getTaskId()).thenReturn(otherId);
        when(otherTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(otherTask.getCostMatrix()).thenReturn(otherMatrix);
        when(otherMatrix.getCosts()).thenReturn("[{\"t\":5, \"c\":-10}]");


        when(module.getTasks()).thenReturn(List.of(currentTask, otherTask));

        @SuppressWarnings("unchecked")
        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
                provider,
                "calculateHeuristiksFromTask",
                currentTask
        );


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(-10, result.get(0).getC());
    }

    @Test
    void calculateHeuristiksFromTask_IgnoresSelf() {

        Task currentTask = mock(Task.class);
        UUID currentId = UUID.randomUUID();
        when(currentTask.getTaskId()).thenReturn(currentId);

        Module module = mock(Module.class);
        when(currentTask.getModule()).thenReturn(module);


        when(module.getTasks()).thenReturn(List.of(currentTask));


        @SuppressWarnings("unchecked")
        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
                provider,
                "calculateHeuristiksFromTask",
                currentTask
        );
        assertTrue(result.isEmpty(), "Sollte leer sein, da man nicht von sich selbst kopieren darf");
    }

    @Test
    void getCostMatrixForTask_NoCostMatrix_CalculatesFromRatings() {
        Task task = mock(Task.class);
        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(task.getCostMatrix()).thenReturn(null);
        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 14, 30));
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.MEDIUM);
        when(rating.getAchievement()).thenReturn(AchievementLevel.PARTIAL);

        List<CostDTO> result = provider.getCostMatrixForTask(task);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCostMatrixForTask_NoCostMatrix_EmptyRatings_CalculatesHeuristics() {
        Task currentTask = mock(Task.class);
        Module module = mock(Module.class);
        Task otherTask = mock(Task.class);
        CostMatrix otherMatrix = mock(CostMatrix.class);

        when(currentTask.getCostMatrix()).thenReturn(null);
        when(currentTask.getLearningUnits()).thenReturn(List.of());
        when(currentTask.getModule()).thenReturn(module);
        when(currentTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(currentTask.getTaskId()).thenReturn(UUID.randomUUID());

        when(otherTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(otherTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(otherTask.getCostMatrix()).thenReturn(otherMatrix);
        when(otherMatrix.getCosts()).thenReturn("[{\"t\":20, \"c\":-3}]");

        when(module.getTasks()).thenReturn(List.of(currentTask, otherTask));

        List<CostDTO> result = provider.getCostMatrixForTask(currentTask);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void calculateCostsFromRatings_AllConcentrationLevels() {
        Task task = mock(Task.class);
        ConcentrationLevel[] levels = {
            ConcentrationLevel.VERY_LOW,
            ConcentrationLevel.LOW,
            ConcentrationLevel.MEDIUM,
            ConcentrationLevel.HIGH,
            ConcentrationLevel.VERY_HIGH
        };

        for (ConcentrationLevel level : levels) {
            LearningUnit unit = mock(LearningUnit.class);
            UnitRating rating = mock(UnitRating.class);

            when(unit.isRated()).thenReturn(true);
            when(unit.getRating()).thenReturn(rating);
            when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));
            when(rating.getConcentration()).thenReturn(level);
            when(rating.getAchievement()).thenReturn(AchievementLevel.GOOD);

            when(task.getLearningUnits()).thenReturn(List.of(unit));
            when(task.getCostMatrix()).thenReturn(null);

            List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
                provider,
                "calculateCostsFromRatings",
                task
            );

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Test
    void calculateCostsFromRatings_AllAchievementLevels() {
        Task task = mock(Task.class);
        AchievementLevel[] levels = {
            AchievementLevel.NONE,
            AchievementLevel.POOR,
            AchievementLevel.PARTIAL,
            AchievementLevel.GOOD,
            AchievementLevel.EXCELLENT
        };

        for (AchievementLevel level : levels) {
            LearningUnit unit = mock(LearningUnit.class);
            UnitRating rating = mock(UnitRating.class);

            when(unit.isRated()).thenReturn(true);
            when(unit.getRating()).thenReturn(rating);
            when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 12, 0));
            when(rating.getConcentration()).thenReturn(ConcentrationLevel.HIGH);
            when(rating.getAchievement()).thenReturn(level);

            when(task.getLearningUnits()).thenReturn(List.of(unit));
            when(task.getCostMatrix()).thenReturn(null);

            List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
                provider,
                "calculateCostsFromRatings",
                task
            );

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Test
    void calculateCostsFromRatings_NullConcentration_UsesDefault() {
        Task task = mock(Task.class);
        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(rating.getConcentration()).thenReturn(null);
        when(rating.getAchievement()).thenReturn(AchievementLevel.GOOD);

        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(task.getCostMatrix()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void calculateCostsFromRatings_NullAchievement_UsesDefault() {
        Task task = mock(Task.class);
        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.HIGH);
        when(rating.getAchievement()).thenReturn(null);

        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(task.getCostMatrix()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void calculateCostsFromRatings_UnitWithoutStartTime_Skipped() {
        Task task = mock(Task.class);
        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(null);
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.HIGH);
        when(rating.getAchievement()).thenReturn(AchievementLevel.GOOD);

        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(task.getCostMatrix()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateCostsFromRatings_EmptyUnits_ReturnsEmpty() {
        Task task = mock(Task.class);

        when(task.getLearningUnits()).thenReturn(List.of());

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateCostsFromRatings_NullUnits_ReturnsEmpty() {
        Task task = mock(Task.class);

        when(task.getLearningUnits()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void applyPenaltyToCostMatrix_TaskNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            provider.applyPenaltyToCostMatrix(null, 10, -5);
        });
        assertEquals("Error: Task darf nicht null sein", exception.getMessage());
    }

    @Test
    void applyPenaltyToCostMatrix_NoCostMatrix_CreatesNewAndAdds() {
        Task task = mock(Task.class);

        when(task.getCostMatrix()).thenReturn(null);

        provider.applyPenaltyToCostMatrix(task, 50, -8);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        verify(costMatrixRepository).save(any(CostMatrix.class));
    }

    @Test
    void applyPenaltyToCostMatrix_ExistingCost_UpdatesCost() {
        Task task = mock(Task.class);
        CostMatrix matrix = new CostMatrix("[{\"t\":10, \"c\":-5}]", task);

        when(task.getCostMatrix()).thenReturn(matrix);

        provider.applyPenaltyToCostMatrix(task, 10, -3);

        verify(costMatrixRepository).save(matrix);
        assertNotNull(matrix.getCosts());
        assertTrue(matrix.getCosts().contains("\"c\":-8"));
    }

    @Test
    void applyPenaltyToCostMatrix_NewCostSlot_AddsNewCost() {
        Task task = mock(Task.class);
        CostMatrix matrix = new CostMatrix("[{\"t\":10, \"c\":-5}]", task);

        when(task.getCostMatrix()).thenReturn(matrix);

        provider.applyPenaltyToCostMatrix(task, 20, -2);

        verify(costMatrixRepository).save(matrix);
        assertNotNull(matrix.getCosts());
    }

    @Test
    void makeCostDTOList_InvalidJson_ReturnsEmpty() {
        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "makeCostDTOList",
            "invalid json"
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void makeCostDTOList_NullJson_ReturnsEmpty() {
        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "makeCostDTOList",
            (String) null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void makeCostDTOList_EmptyJson_ReturnsEmpty() {
        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "makeCostDTOList",
            ""
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateHeuristiksFromTask_NullModule_ReturnsEmpty() {
        Task task = mock(Task.class);

        when(task.getModule()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateHeuristiksFromTask",
            task
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateHeuristiksFromTask_NullTasks_ReturnsEmpty() {
        Task task = mock(Task.class);
        Module module = mock(Module.class);

        when(task.getModule()).thenReturn(module);
        when(module.getTasks()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateHeuristiksFromTask",
            task
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateHeuristiksFromTask_EmptyTasks_ReturnsEmpty() {
        Task task = mock(Task.class);
        Module module = mock(Module.class);

        when(task.getModule()).thenReturn(module);
        when(module.getTasks()).thenReturn(List.of());

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateHeuristiksFromTask",
            task
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateHeuristiksFromTask_DifferentCategory_SkipsTask() {
        Task currentTask = mock(Task.class);
        Task otherTask = mock(Task.class);
        Module module = mock(Module.class);

        when(currentTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(currentTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(currentTask.getModule()).thenReturn(module);

        when(otherTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(otherTask.getCategory()).thenReturn(TaskCategory.OTHER);

        when(module.getTasks()).thenReturn(List.of(currentTask, otherTask));

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateHeuristiksFromTask",
            currentTask
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateHeuristiksFromTask_NoCostMatrix_SkipsTask() {
        Task currentTask = mock(Task.class);
        Task otherTask = mock(Task.class);
        Module module = mock(Module.class);

        when(currentTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(currentTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(currentTask.getModule()).thenReturn(module);

        when(otherTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(otherTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(otherTask.getCostMatrix()).thenReturn(null);

        when(module.getTasks()).thenReturn(List.of(currentTask, otherTask));

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateHeuristiksFromTask",
            currentTask
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateHeuristiksFromTask_CostMatrixNullCosts_SkipsTask() {
        Task currentTask = mock(Task.class);
        Task otherTask = mock(Task.class);
        CostMatrix costMatrix = mock(CostMatrix.class);
        Module module = mock(Module.class);

        when(currentTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(currentTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(currentTask.getModule()).thenReturn(module);

        when(otherTask.getTaskId()).thenReturn(UUID.randomUUID());
        when(otherTask.getCategory()).thenReturn(TaskCategory.EXAM);
        when(otherTask.getCostMatrix()).thenReturn(costMatrix);
        when(costMatrix.getCosts()).thenReturn(null);

        when(module.getTasks()).thenReturn(List.of(currentTask, otherTask));

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateHeuristiksFromTask",
            currentTask
        );

        assertTrue(result.isEmpty());
    }

    // ========== TESTS FÜR SCHWER ERREICHBARE BRANCHES ==========

    @Test
    void calculateCostsFromRatings_UnitNotRated_Skipped() {
        Task task = mock(Task.class);
        LearningUnit unit = mock(LearningUnit.class);

        when(unit.isRated()).thenReturn(false);
        when(task.getLearningUnits()).thenReturn(List.of(unit));

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertTrue(result.isEmpty(), "Non-rated unit should be skipped");
    }

    @Test
    void calculateCostsFromRatings_MultipleUnitsWithMixedRatings() {
        Task task = mock(Task.class);
        LearningUnit ratedUnit = mock(LearningUnit.class);
        LearningUnit unratedUnit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(ratedUnit.isRated()).thenReturn(true);
        when(ratedUnit.getRating()).thenReturn(rating);
        when(ratedUnit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.HIGH);
        when(rating.getAchievement()).thenReturn(AchievementLevel.GOOD);

        when(unratedUnit.isRated()).thenReturn(false);

        when(task.getLearningUnits()).thenReturn(List.of(ratedUnit, unratedUnit));

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertEquals(1, result.size(), "Only rated units should be included");
    }

    @Test
    void applyPenaltyToCostMatrix_MultiplePenalties_SortedBySlot() {
        Task task = mock(Task.class);
        CostMatrix matrix = new CostMatrix("[]", task);

        when(task.getCostMatrix()).thenReturn(matrix);

        provider.applyPenaltyToCostMatrix(task, 50, -5);
        provider.applyPenaltyToCostMatrix(task, 10, -3);
        provider.applyPenaltyToCostMatrix(task, 30, -2);

        verify(costMatrixRepository, times(3)).save(matrix);
        assertNotNull(matrix.getCosts());
    }

    @Test
    void calculateCostsFromRatings_WithNullCostMatrixInTask() {
        Task task = mock(Task.class);
        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 15, 30));
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.VERY_HIGH);
        when(rating.getAchievement()).thenReturn(AchievementLevel.EXCELLENT);

        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(task.getCostMatrix()).thenReturn(null);

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "calculateCostsFromRatings",
            task
        );

        assertEquals(1, result.size());
    }

    @Test
    void getCostMatrixForTask_MatrixOutdated_WithoutPersist() {
        Task task = mock(Task.class);
        CostMatrix matrix = new CostMatrix();
        matrix.markAsOutdated();

        LearningUnit unit = mock(LearningUnit.class);
        UnitRating rating = mock(UnitRating.class);

        when(task.getCostMatrix()).thenReturn(matrix);
        when(task.getLearningUnits()).thenReturn(List.of(unit));
        when(unit.isRated()).thenReturn(true);
        when(unit.getRating()).thenReturn(rating);
        when(unit.getStartTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 8, 15));
        when(rating.getConcentration()).thenReturn(ConcentrationLevel.LOW);
        when(rating.getAchievement()).thenReturn(AchievementLevel.POOR);

        List<CostDTO> result = provider.getCostMatrixForTask(task);

        assertNotNull(result);
        verify(costMatrixRepository).save(any());
    }

    @Test
    void makeCostDTOList_ValidJson_ReturnsList() {
        String validJson = "[{\"t\":10, \"c\":-5}, {\"t\":20, \"c\":-3}]";

        List<CostDTO> result = (List<CostDTO>) ReflectionTestUtils.invokeMethod(
            provider,
            "makeCostDTOList",
            validJson
        );

        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getT());
        assertEquals(-5, result.get(0).getC());
    }

    @Test
    void applyPenaltyToCostMatrix_EmptyMatrix_AddsFirstPenalty() {
        Task task = mock(Task.class);
        CostMatrix matrix = new CostMatrix("[]", task);

        when(task.getCostMatrix()).thenReturn(matrix);

        provider.applyPenaltyToCostMatrix(task, 100, -7);

        verify(costMatrixRepository).save(matrix);
        assertNotNull(matrix.getCosts());
        assertTrue(matrix.getCosts().contains("\"t\":100"));
    }
}
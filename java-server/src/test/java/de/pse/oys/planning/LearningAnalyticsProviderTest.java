package de.pse.oys.planning;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.CostMatrix;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.UnitRating;
import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.domain.enums.TaskCategory; // Annahme: Enum existiert
import de.pse.oys.dto.CostDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.service.planning.LearningAnalyticsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void getCostMatrixForTask_MatrixOutdated_CalculatesFromRatingsAndPersists() throws JsonProcessingException {

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
        // Setup: Nur der Task selbst ist im Modul
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
}
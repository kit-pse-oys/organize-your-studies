package de.pse.oys.rating;

import de.pse.oys.domain.*;
import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.domain.enums.PerceivedDuration;
import de.pse.oys.dto.RatingDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RatingServiceTest – Unit-Tests für den RatingService.
 * Testet das Speichern von Bewertungen und die Handhabung von Fehlerfällen.
 *
 * @author uhupo
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class RatingServiceTest {

    private LearningUnitRepository learningUnitRepository;
    private CostMatrixRepository costMatrixRepository;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        learningUnitRepository = mock(LearningUnitRepository.class);
        costMatrixRepository = mock(CostMatrixRepository.class);

        ratingService = new RatingService(learningUnitRepository, costMatrixRepository);
    }

    @Test
    void submitRating_shouldSaveRatingAndMarkCostMatrixOutdated() {
        // Arrange
        UUID learningUnitId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        // Task mocken
        Task task = mock(Task.class);
        when(task.getTaskId()).thenReturn(taskId);

        // LearningUnit direkt mit Konstruktor erstellen
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        LearningUnit unit = new LearningUnit(task, start, end);

        // CostMatrix erstellen und Task zuweisen
        String initialCosts = "{}"; // Dummy JSON
        CostMatrix costMatrix = new CostMatrix(initialCosts, task);

        // RatingDTO erstellen
        RatingDTO ratingDTO = new RatingDTO(AchievementLevel.GOOD, PerceivedDuration.IDEAL, ConcentrationLevel.VERY_HIGH);

        when(learningUnitRepository.findById(learningUnitId)).thenReturn(Optional.of(unit));
        when(costMatrixRepository.findByTask_TaskId(taskId)).thenReturn(Optional.of(costMatrix));

        // Act
        ratingService.submitRating(learningUnitId, ratingDTO);

        // Assert
        // LearningUnit wurde gespeichert
        ArgumentCaptor<LearningUnit> learningUnitCaptor = ArgumentCaptor.forClass(LearningUnit.class);
        verify(learningUnitRepository).save(learningUnitCaptor.capture());
        LearningUnit savedUnit = learningUnitCaptor.getValue();
        assertNotNull(savedUnit.getRating(), "UnitRating sollte gesetzt sein");
        assertEquals(ConcentrationLevel.VERY_HIGH, savedUnit.getRating().getConcentration());
        assertEquals(PerceivedDuration.IDEAL, savedUnit.getRating().getPerceivedDuration());
        assertEquals(AchievementLevel.GOOD, savedUnit.getRating().getAchievement());

        // CostMatrix wurde gespeichert und als outdated markiert
        ArgumentCaptor<CostMatrix> costMatrixCaptor = ArgumentCaptor.forClass(CostMatrix.class);
        verify(costMatrixRepository).save(costMatrixCaptor.capture());
        CostMatrix savedMatrix = costMatrixCaptor.getValue();
        assertTrue(savedMatrix.isOutdated(), "CostMatrix sollte als veraltet markiert sein");
    }

    @Test
    void submitRating_shouldThrowException_whenLearningUnitNotFound() {
        UUID randomId = UUID.randomUUID();
        when(learningUnitRepository.findById(randomId)).thenReturn(Optional.empty());

        RatingDTO ratingDTO = new RatingDTO(AchievementLevel.GOOD, PerceivedDuration.IDEAL, ConcentrationLevel.VERY_HIGH);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ratingService.submitRating(randomId, ratingDTO));

        assertTrue(ex.getMessage().contains("Es wurde keine Lerneinheit"));
    }

    @Test
    void submitRating_shouldThrowException_whenCostMatrixNotFound() {
        UUID learningUnitId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Task task = mock(Task.class);
        when(task.getTaskId()).thenReturn(taskId);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        LearningUnit unit = new LearningUnit(task, start, end);

        when(learningUnitRepository.findById(learningUnitId)).thenReturn(Optional.of(unit));
        when(costMatrixRepository.findByTask_TaskId(taskId)).thenReturn(Optional.empty());

        RatingDTO ratingDTO = new RatingDTO(AchievementLevel.GOOD, PerceivedDuration.IDEAL, ConcentrationLevel.VERY_HIGH);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ratingService.submitRating(learningUnitId, ratingDTO));

        assertTrue(ex.getMessage().contains("Keine CostMatrix"));
    }
}
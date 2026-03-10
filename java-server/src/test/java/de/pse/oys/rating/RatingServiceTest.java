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
    void markAsMissed_shouldSetStatusToMissed() {
        // Arrange
        UUID unitId = UUID.randomUUID();
        LearningUnit unit = mock(LearningUnit.class); // Mock reicht hier, um Interaktion zu prüfen
        when(learningUnitRepository.findById(unitId)).thenReturn(Optional.of(unit));

        // Act
        ratingService.markAsMissed(unitId);

        // Assert
        verify(unit).markAsMissed();
        verify(learningUnitRepository).save(unit);
    }

    @Test
    void getRateableUnits_shouldFilterCorrectUnits() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Einheit 1: Valide (Vergangen, kein Rating, nicht MISSED)
        LearningUnit validUnit = mock(LearningUnit.class);
        when(validUnit.getUnitId()).thenReturn(UUID.randomUUID());
        when(validUnit.getRating()).thenReturn(null);
        when(validUnit.getStatus()).thenReturn(null); // Default Status
        when(validUnit.hasPassed()).thenReturn(true);

        // Einheit 2: Bereits bewertet (sollte gefiltert werden)
        LearningUnit ratedUnit = mock(LearningUnit.class);
        when(ratedUnit.getRating()).thenReturn(mock(UnitRating.class));

        // Einheit 3: Verpasst (sollte gefiltert werden)
        LearningUnit missedUnit = mock(LearningUnit.class);
        when(missedUnit.getRating()).thenReturn(null);
        when(missedUnit.getStatus()).thenReturn(de.pse.oys.domain.enums.UnitStatus.MISSED);

        // Einheit 4: Noch in der Zukunft (sollte gefiltert werden)
        LearningUnit futureUnit = mock(LearningUnit.class);
        when(futureUnit.getRating()).thenReturn(null);
        when(futureUnit.hasPassed()).thenReturn(false);

        when(learningUnitRepository.findAllByTask_Module_User_UserId(userId))
                .thenReturn(java.util.List.of(validUnit, ratedUnit, missedUnit, futureUnit));

        // Act
        java.util.List<UUID> result = ratingService.getRateableUnits(userId);

        // Assert
        assertEquals(1, result.size(), "Nur eine Einheit sollte übrig bleiben");
        assertEquals(validUnit.getUnitId(), result.get(0));
    }
}
package de.pse.oys.learningUnit;

import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.UnitStatus;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.service.LearningUnitService;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningUnitServiceTest {

    private static final UUID USER_ID  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID UNIT_ID  = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private LearningPlanRepository learningPlanRepository;

    @Mock
    private LearningUnitRepository learningUnitRepository;

    @InjectMocks
    private LearningUnitService sut;

    private LearningPlan plan;
    private LearningUnit unit;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Da User abstrakt ist, erstellen wir eine anonyme Unterklasse
        testUser = new User("TestUser", UserType.LOCAL) {};
        setField(testUser, "userId", USER_ID); // Setzt die ID für p.getUser().getId()

        // Plan mit dem öffentlichen Konstruktor (start, end)
        plan = new LearningPlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));
        plan.setUserId(testUser.getId()); // Verknüpfung für den Service-Check

        // Unit vorbereiten
        unit = unitWithTaskAndModule(
                "Test Task",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0)
        );
        setField(unit, "unitId", UNIT_ID);

        // Liste initialisieren und Unit hinzufügen
        setField(plan, "units", new ArrayList<>(List.of(unit)));
    }

    @Test
    @DisplayName("Verschiebt eine Lerneinheit erfolgreich auf einen neuen Startzeitpunkt")
    void moveLearningUnitManually_Success() {
        // GIVEN
        LocalDateTime newStart = LocalDateTime.of(2026, 1, 2, 14, 0);
        // Dauer der Original-Unit war 1 Stunde (10:00 - 11:00)
        LocalDateTime expectedEnd = newStart.plusHours(1);

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        // WHEN
        sut.moveLearningUnitManually(USER_ID, UNIT_ID, newStart);

        // THEN
        assertThat(unit.getStartTime()).isEqualTo(newStart);
        assertThat(unit.getEndTime()).isEqualTo(expectedEnd);
        verify(learningPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn die verschobene Einheit mit einer anderen überlappt")
    void moveLearningUnitManually_ThrowsOnOverlap() {
        // GIVEN
        // Eine zweite Einheit im Plan erstellen, die von 14:00 bis 15:00 geht
        LearningUnit secondUnit = unitWithTaskAndModule(
                "Other Task",
                LocalDateTime.of(2026, 1, 1, 14, 0),
                LocalDateTime.of(2026, 1, 1, 15, 0)
        );
        setField(secondUnit, "unitId", UUID.randomUUID());
        ((List<LearningUnit>) plan.getUnits()).add(secondUnit);

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        // Versuch, die erste Einheit genau auf den Zeitraum der zweiten zu schieben
        LocalDateTime overlappingStart = LocalDateTime.of(2026, 1, 1, 14, 30);

        // WHEN & THEN
        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, UNIT_ID, overlappingStart))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Die Einheit überschneidet sich");
    }

    // -------------------------------------------------------------------------
    // finishUnitEarly
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Wirft ValidationException bei negativer tatsächlicher Dauer")
    void finishUnitEarly_ThrowsOnNegativeDuration() {
        // WHEN & THEN
        assertThatThrownBy(() -> sut.finishUnitEarly(USER_ID, UNIT_ID, -5))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Dauer muss >= 0 sein");
    }

    @Test
    @DisplayName("Markiert die Einheit als vorzeitig abgeschlossen und speichert die tatsächliche Dauer")
    void finishUnitEarly_UpdatesStatus() {
        // GIVEN
        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        // WHEN
        sut.finishUnitEarly(USER_ID, UNIT_ID, 30);

        // THEN
        assertThat(unit.getStatus().equals(UnitStatus.COMPLETED)).isTrue();
        verify(learningPlanRepository).save(plan);
    }

    // -------------------------------------------------------------------------
    // getLearningUnitsByUserId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Liefert alle Lerneinheiten eines Users als gemappte WrapperDTOs zurück")
    void getLearningUnitsByUserId_ReturnsMappedDtos() {
        // GIVEN
        when(learningUnitRepository.findAllByTask_Module_User_UserId(USER_ID)).thenReturn(List.of(unit));

        // WHEN
        List<WrapperDTO<UnitDTO>> result = sut.getLearningUnitsByUserId(USER_ID);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(UNIT_ID);
        assertThat(result.get(0).getData()).isNotNull();
        // Hier wird indirekt die toDTO() Methode der Entity getestet
    }

    // -------------------------------------------------------------------------
    // Security / Ownership
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Wirft ResourceNotFoundException, wenn die Unit nicht zum User gehört")
    void findPlanByUnitAndUser_ThrowsWhenNotFound() {
        // GIVEN
        UUID strangerId = UUID.randomUUID();
        when(learningPlanRepository.findAll()).thenReturn(List.of(plan)); // Plan gehört USER_ID

        // WHEN & THEN
        assertThatThrownBy(() -> sut.finishUnitEarly(strangerId, UNIT_ID, 30))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kein passender Lernplan");
    }

    // --- Hilfsmethoden ---

    private static LearningUnit unitWithTaskAndModule(String title, LocalDateTime start, LocalDateTime end) {
        Task task = new Task(title, 1, TaskCategory.OTHER) {
            @Override public LocalDateTime getHardDeadline() { return null; }
            @Override public boolean isActive() { return true; }
        };
        return new LearningUnit(task, start, end);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            if (f == null) { // Check für Superklassen (z.B. User/Task IDs)
                f = target.getClass().getSuperclass().getDeclaredField(fieldName);
            }
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            // Fallback für tiefer liegende Superklassen
            try {
                Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
            } catch (Exception ex) {
                throw new RuntimeException("Field " + fieldName + " not found", ex);
            }
        }
    }
}
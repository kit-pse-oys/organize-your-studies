package de.pse.oys.learningUnit;

import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.UnitStatus;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.service.LearningUnitService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningUnitServiceTest {

    private static final UUID USER_ID  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID UNIT_ID  = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private LearningPlanRepository learningPlanRepository;

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
        plan.setUser(testUser); // Verknüpfung für den Service-Check

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
    void moveUnitAutomatically_ReturnsCorrectWrapperData() {
        // GIVEN
        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        // WHEN
        UnitDTO result = sut.moveLearningUnitAutomatically(USER_ID, UNIT_ID);

        // THEN
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(learningPlanRepository).save(plan);
    }

    @Test
    void finishUnitEarly_UpdatesStatus() {
        // GIVEN
        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        // WHEN
        sut.finishUnitEarly(USER_ID, UNIT_ID, 30);

        // THEN
        assertThat(unit.getStatus().equals(UnitStatus.COMPLETED)).isTrue();
        verify(learningPlanRepository).save(plan);
    }

    // --- Hilfsmethoden ---

    private static LearningUnit unitWithTaskAndModule(String title, LocalDateTime start, LocalDateTime end) {
        Task task = new Task(title, 1, TaskCategory.OTHER) {
            @Override public LocalDateTime getHardDeadline() { return null; }
            @Override protected boolean isActive() { return true; }
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
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningUnitServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID UNIT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

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
        testUser = new User("TestUser", UserType.LOCAL) {};
        setField(testUser, "userId", USER_ID);

        plan = new LearningPlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));
        plan.setUserId(testUser.getId());

        unit = unitWithTaskAndModule(
                "Test Task",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0)
        );
        setField(unit, "unitId", UNIT_ID);

        setField(plan, "units", new ArrayList<>(List.of(unit)));
    }

    @Test
    @DisplayName("Verschiebt eine Lerneinheit erfolgreich auf einen neuen Startzeitpunkt")
    void moveLearningUnitManually_Success() {
        LocalDateTime newStart = LocalDateTime.of(2026, 1, 2, 14, 0);
        LocalDateTime expectedEnd = newStart.plusHours(1);

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        sut.moveLearningUnitManually(USER_ID, UNIT_ID, newStart);

        assertThat(unit.getStartTime()).isEqualTo(newStart);
        assertThat(unit.getEndTime()).isEqualTo(expectedEnd);
        verify(learningPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn userId beim manuellen Verschieben null ist")
    void moveLearningUnitManually_ThrowsWhenUserIdNull() {
        LocalDateTime newStart = LocalDateTime.of(2026, 1, 2, 14, 0);

        assertThatThrownBy(() -> sut.moveLearningUnitManually(null, UNIT_ID, newStart))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Pflichtfelder fehlen");

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn unitId beim manuellen Verschieben null ist")
    void moveLearningUnitManually_ThrowsWhenUnitIdNull() {
        LocalDateTime newStart = LocalDateTime.of(2026, 1, 2, 14, 0);

        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, null, newStart))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Pflichtfelder fehlen");

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn der neue Startzeitpunkt null ist")
    void moveLearningUnitManually_ThrowsWhenStartNull() {
        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, UNIT_ID, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Pflichtfelder fehlen");

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn die verschobene Einheit mit einer anderen überlappt")
    void moveLearningUnitManually_ThrowsOnOverlap() {
        LearningUnit secondUnit = unitWithTaskAndModule(
                "Other Task",
                LocalDateTime.of(2026, 1, 1, 14, 0),
                LocalDateTime.of(2026, 1, 1, 15, 0)
        );
        setField(secondUnit, "unitId", UUID.randomUUID());
        ((List<LearningUnit>) plan.getUnits()).add(secondUnit);

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        LocalDateTime overlappingStart = LocalDateTime.of(2026, 1, 1, 14, 30);

        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, UNIT_ID, overlappingStart))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Die Einheit überschneidet sich");
    }

    @Test
    @DisplayName("Erlaubt angrenzende Einheiten ohne Überschneidung beim manuellen Verschieben")
    void moveLearningUnitManually_AllowsAdjacentUnitWithoutOverlap() {
        LearningUnit secondUnit = unitWithTaskAndModule(
                "Other Task",
                LocalDateTime.of(2026, 1, 1, 14, 0),
                LocalDateTime.of(2026, 1, 1, 15, 0)
        );
        setField(secondUnit, "unitId", UUID.randomUUID());
        ((List<LearningUnit>) plan.getUnits()).add(secondUnit);

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        LocalDateTime adjacentStart = LocalDateTime.of(2026, 1, 1, 13, 0);

        sut.moveLearningUnitManually(USER_ID, UNIT_ID, adjacentStart);

        assertThat(unit.getStartTime()).isEqualTo(adjacentStart);
        assertThat(unit.getEndTime()).isEqualTo(LocalDateTime.of(2026, 1, 1, 14, 0));
        verify(learningPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Ignoriert null-Einträge und Einträge ohne ID bei der Overlap-Prüfung")
    void moveLearningUnitManually_IgnoresNullAndUnitsWithoutIdInOverlapCheck() {
        LearningUnit noIdUnit = unitWithTaskAndModule(
                "No Id",
                LocalDateTime.of(2026, 1, 1, 14, 0),
                LocalDateTime.of(2026, 1, 1, 15, 0)
        );

        List<LearningUnit> units = new ArrayList<>();
        units.add(unit);
        units.add(null);
        units.add(noIdUnit);
        setField(plan, "units", units);

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        LocalDateTime newStart = LocalDateTime.of(2026, 1, 1, 12, 0);

        sut.moveLearningUnitManually(USER_ID, UNIT_ID, newStart);

        assertThat(unit.getStartTime()).isEqualTo(newStart);
        assertThat(unit.getEndTime()).isEqualTo(LocalDateTime.of(2026, 1, 1, 13, 0));
        verify(learningPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Wirft ResourceNotFoundException, wenn kein passender Lernplan für die Unit gefunden wird")
    void moveLearningUnitManually_ThrowsWhenUnitNotFoundInPlan() {
        setField(plan, "units", new ArrayList<>());

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        LocalDateTime newStart = LocalDateTime.of(2026, 1, 2, 14, 0);

        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, UNIT_ID, newStart))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kein passender Lernplan");
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn die Unit eine ungültige Dauer von 0 Minuten hat")
    void moveLearningUnitManually_ThrowsWhenExistingUnitHasZeroDuration() {
        unit.setEndTime(unit.getStartTime());

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        LocalDateTime newStart = LocalDateTime.of(2026, 1, 2, 14, 0);

        assertThatThrownBy(() -> sut.moveLearningUnitManually(USER_ID, UNIT_ID, newStart))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Startzeit muss vor der Endzeit liegen");
    }

    // -------------------------------------------------------------------------
    // finishUnitEarly
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Wirft ValidationException, wenn userId beim vorzeitigen Abschließen null ist")
    void finishUnitEarly_ThrowsWhenUserIdNull() {
        assertThatThrownBy(() -> sut.finishUnitEarly(null, UNIT_ID, 30))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Pflichtfelder fehlen");

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn unitId beim vorzeitigen Abschließen null ist")
    void finishUnitEarly_ThrowsWhenUnitIdNull() {
        assertThatThrownBy(() -> sut.finishUnitEarly(USER_ID, null, 30))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Pflichtfelder fehlen");

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    @DisplayName("Wirft ValidationException, wenn actualDuration beim vorzeitigen Abschließen null ist")
    void finishUnitEarly_ThrowsWhenActualDurationNull() {
        assertThatThrownBy(() -> sut.finishUnitEarly(USER_ID, UNIT_ID, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Pflichtfelder fehlen");

        verifyNoInteractions(learningPlanRepository);
    }

    @Test
    @DisplayName("Wirft ValidationException bei negativer tatsächlicher Dauer")
    void finishUnitEarly_ThrowsOnNegativeDuration() {
        assertThatThrownBy(() -> sut.finishUnitEarly(USER_ID, UNIT_ID, -5))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Dauer muss >= 0 sein");
    }

    @Test
    @DisplayName("Markiert die Einheit als vorzeitig abgeschlossen und speichert die tatsächliche Dauer")
    void finishUnitEarly_UpdatesStatus() {
        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        sut.finishUnitEarly(USER_ID, UNIT_ID, 30);

        assertThat(unit.getStatus()).isEqualTo(UnitStatus.COMPLETED);
        verify(learningPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Wirft ResourceNotFoundException, wenn kein passender Lernplan für die Unit gefunden wird")
    void finishUnitEarly_ThrowsWhenUnitNotFoundInPlan() {
        setField(plan, "units", new ArrayList<>());

        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        assertThatThrownBy(() -> sut.finishUnitEarly(USER_ID, UNIT_ID, 30))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kein passender Lernplan");
    }

    // -------------------------------------------------------------------------
    // getLearningUnitsByUserId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Liefert alle Lerneinheiten eines Users als gemappte WrapperDTOs zurück")
    void getLearningUnitsByUserId_ReturnsMappedDtos() {
        when(learningUnitRepository.findAllByTask_Module_User_UserId(USER_ID)).thenReturn(List.of(unit));

        List<WrapperDTO<UnitDTO>> result = sut.getLearningUnitsByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(UNIT_ID);
        assertThat(result.get(0).getData()).isNotNull();
    }

    @Test
    @DisplayName("Liefert leere Liste, wenn der User keine Lerneinheiten hat")
    void getLearningUnitsByUserId_ReturnsEmptyList() {
        when(learningUnitRepository.findAllByTask_Module_User_UserId(USER_ID)).thenReturn(List.of());

        List<WrapperDTO<UnitDTO>> result = sut.getLearningUnitsByUserId(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Wirft NullPointerException, wenn userId beim Laden der Lerneinheiten null ist")
    void getLearningUnitsByUserId_ThrowsWhenUserIdNull() {
        assertThatThrownBy(() -> sut.getLearningUnitsByUserId(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -------------------------------------------------------------------------
    // Security / Ownership
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Wirft ResourceNotFoundException, wenn die Unit nicht zum User gehört")
    void findPlanByUnitAndUser_ThrowsWhenNotFound() {
        UUID strangerId = UUID.randomUUID();
        when(learningPlanRepository.findAll()).thenReturn(List.of(plan));

        assertThatThrownBy(() -> sut.finishUnitEarly(strangerId, UNIT_ID, 30))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kein passender Lernplan");
    }

    // -------------------------------------------------------------------------
    // Reflection-Tests für intern noch nicht erreichte Branches
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findUnitOrThrow wirft ResourceNotFoundException, wenn die Unit im Plan fehlt")
    void findUnitOrThrow_ThrowsWhenMissing() throws Exception {
        LearningPlan emptyPlan = new LearningPlan(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));
        emptyPlan.setUserId(USER_ID);
        setField(emptyPlan, "units", new ArrayList<>());

        Method method = LearningUnitService.class.getDeclaredMethod("findUnitOrThrow", LearningPlan.class, UUID.class);
        method.setAccessible(true);

        assertThatThrownBy(() -> invoke(method, sut, emptyPlan, UNIT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("LearningUnit existiert nicht");
    }

    @Test
    @DisplayName("moveUnitInternal wirft ValidationException bei identischem Start- und Endzeitpunkt")
    void moveUnitInternal_ThrowsWhenStartEqualsEnd() throws Exception {
        Method method = LearningUnitService.class.getDeclaredMethod(
                "moveUnitInternal",
                LearningPlan.class,
                LearningUnit.class,
                LocalDateTime.class,
                LocalDateTime.class
        );
        method.setAccessible(true);

        LocalDateTime same = LocalDateTime.of(2026, 1, 3, 12, 0);

        assertThatThrownBy(() -> invoke(method, sut, plan, unit, same, same))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Startzeit muss vor der Endzeit liegen");
    }

    // --- Hilfsmethoden ---

    private static LearningUnit unitWithTaskAndModule(String title, LocalDateTime start, LocalDateTime end) {
        Task task = new Task(title, 1, TaskCategory.OTHER) {
            @Override
            public LocalDateTime getHardDeadline() {
                return null;
            }

            @Override
            public boolean isActive() {
                return true;
            }
        };
        return new LearningUnit(task, start, end);
    }

    private static Object invoke(Method method, Object target, Object... args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        Class<?> current = target.getClass();

        while (current != null) {
            try {
                Field f = current.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field " + fieldName, e);
            }
        }

        throw new RuntimeException("Field " + fieldName + " not found");
    }
}
package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service für Änderungen an bestehenden Lerneinheiten.
 *
 * @author uqvfm
 * @version 1.0
 */
@Service
@Transactional
public class LearningUnitService {

    private static final int MINUTES_LOWER_BOUND = 0;
    private static final int DAY_OF_WEEK_VALUE_MONDAY = 1;

    private static final String METHOD_GET_USER_ID = "getUserId";
    private static final String METHOD_GET_USER = "getUser";
    private static final String METHOD_GET_ID = "getId";

    private static final String MSG_DTO_NULL = "dto must not be null";

    private static final String MSG_PLAN_NOT_FOUND_TEMPLATE = "LearningPlan not found: %s";
    private static final String MSG_UNIT_NOT_IN_PLAN_TEMPLATE =
            "LearningUnit %s is not part of LearningPlan %s";

    private static final String MSG_START_END_NULL = "start and end must not be null";
    private static final String MSG_END_NOT_AFTER_START = "end must be after start";

    private static final String MSG_ACTUAL_DURATION_NEGATIVE = "actualDuration must be >= 0";
    private static final String MSG_START_TIME_NULL_CANNOT_FINISH_EARLY =
            "Cannot finish unit early: startTime is null";

    private static final String MSG_PLAN_NOT_OWNED_TEMPLATE = "LearningPlan does not belong to user: %s";

    private static final String METHOD_SET_PERSISTED = "setPersisted";
    private static final String METHOD_SET_MANUAL_OVERRIDE = "setManualOverride";
    private static final String METHOD_SET_MANUALLY_MOVED = "setManuallyMoved";
    private static final String METHOD_SET_LOCKED = "setLocked";
    private static final String METHOD_SET_FIXED = "setFixed";

    private final LearningPlanRepository learningPlanRepository;
    private final TaskRepository taskRepository;

    public LearningUnitService(LearningPlanRepository learningPlanRepository, TaskRepository taskRepository) {
        this.learningPlanRepository = learningPlanRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Aktualisiert eine bestehende Lerneinheit anhand eines {@link UnitDTO}.
     *
     * @param userId die ID des Nutzers (Ownership wird geprüft, falls das Domainmodell es hergibt)
     * @param planId die ID des Lernplans (Woche), in dem die Einheit liegt
     * @param unitId die ID der zu ändernden Lerneinheit
     * @param dto    neue Kalenderdaten
     * @return der aktualisierte Lernplan als DTO
     */
    public LearningPlanDTO updateLearningUnit(UUID userId, UUID planId, UUID unitId, UnitDTO dto) {
        Objects.requireNonNull(dto, MSG_DTO_NULL);

        LearningPlan plan = requirePlan(planId);
        assertPlanOwnedByUserIfPossible(plan, userId);

        LearningUnit unit = requireUnitInPlan(plan, unitId);

        // Zeitfenster aktualisieren (nur wenn Datum + Start + Ende gesetzt sind)
        if (dto.getDate() != null && dto.getStart() != null && dto.getEnd() != null) {
            LocalDateTime start = LocalDateTime.of(dto.getDate(), dto.getStart());
            LocalDateTime end = LocalDateTime.of(dto.getDate(), dto.getEnd());
            validateTimeRange(start, end);
            unit.setStartTime(start);
            unit.setEndTime(end);
        }

        // Titeländerung wirkt fachlich auf die Aufgabe (nicht auf die Unit selbst).
        if (dto.getTitle() != null && !dto.getTitle().isBlank() && unit.getTask() != null) {
            Task task = unit.getTask();
            task.setTitle(dto.getTitle());
            taskRepository.save(task);
        }

        learningPlanRepository.save(plan);
        return mapToLearningPlanDTO(plan);
    }

    /**
     * Verschiebt eine Lerneinheit manuell (z. B. Drag-and-Drop im Kalender).
     */
    public LearningPlanDTO moveLearningUnitManually(UUID userId, UUID planId, UUID unitId, LocalDateTime start, LocalDateTime end) {
        LearningPlan plan = requirePlan(planId);
        assertPlanOwnedByUserIfPossible(plan, userId);

        LearningUnit unit = requireUnitInPlan(plan, unitId);
        validateTimeRange(start, end);

        unit.setStartTime(start);
        unit.setEndTime(end);

        setManualPersistFlagIfPresent(unit);

        learningPlanRepository.save(plan);
        return mapToLearningPlanDTO(plan);
    }

    /**
     * Markiert eine Lerneinheit als vorzeitig abgeschlossen und speichert die tatsächliche Dauer.
     */
    public LearningPlanDTO finishUnitEarly(UUID userId, UUID planId, UUID unitId, Integer actualDuration) {
        LearningPlan plan = requirePlan(planId);
        assertPlanOwnedByUserIfPossible(plan, userId);

        LearningUnit unit = requireUnitInPlan(plan, unitId);

        int minutes;
        if (actualDuration != null) {
            if (actualDuration < MINUTES_LOWER_BOUND) {
                throw new IllegalArgumentException(MSG_ACTUAL_DURATION_NEGATIVE);
            }
            minutes = actualDuration;
        } else {
            LocalDateTime start = unit.getStartTime();
            if (start == null) {
                throw new IllegalArgumentException(MSG_START_TIME_NULL_CANNOT_FINISH_EARLY);
            }
            LocalDateTime now = LocalDateTime.now();
            long diff = java.time.Duration.between(start, now).toMinutes();
            minutes = (int) Math.max(MINUTES_LOWER_BOUND, diff);
        }

        unit.markAsCompletedEarly(minutes);

        learningPlanRepository.save(plan);
        return mapToLearningPlanDTO(plan);
    }

    // -------------------------------------------------------------------------
    // Laden / Validieren
    // -------------------------------------------------------------------------

    private LearningPlan requirePlan(UUID planId) {
        return learningPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_PLAN_NOT_FOUND_TEMPLATE, planId)));
    }

    private LearningUnit requireUnitInPlan(LearningPlan plan, UUID unitId) {
        for (LearningUnit u : safeList(plan.getUnits())) {
            if (u != null && unitId.equals(u.getUnitId())) {
                return u;
            }
        }
        throw new IllegalArgumentException(String.format(MSG_UNIT_NOT_IN_PLAN_TEMPLATE, unitId, plan.getPlanId()));
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(MSG_START_END_NULL);
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(MSG_END_NOT_AFTER_START);
        }
    }

    private void assertPlanOwnedByUserIfPossible(LearningPlan plan, UUID userId) {
        if (plan == null || userId == null) {
            return;
        }

        UUID planUserId = tryInvokeUuidGetter(plan, METHOD_GET_USER_ID);
        if (planUserId != null) {
            if (!userId.equals(planUserId)) {
                throw new IllegalArgumentException(String.format(MSG_PLAN_NOT_OWNED_TEMPLATE, userId));
            }
            return;
        }

        Object userObj = tryInvokeGetter(plan, METHOD_GET_USER);
        if (userObj == null) {
            return;
        }

        UUID extractedUserId = tryInvokeUuidGetter(userObj, METHOD_GET_ID);
        if (extractedUserId == null) {
            extractedUserId = tryInvokeUuidGetter(userObj, METHOD_GET_USER_ID);
        }

        if (extractedUserId != null && !userId.equals(extractedUserId)) {
            throw new IllegalArgumentException(String.format(MSG_PLAN_NOT_OWNED_TEMPLATE, userId));
        }
    }

    private Object tryInvokeGetter(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private UUID tryInvokeUuidGetter(Object target, String methodName) {
        Object val = tryInvokeGetter(target, methodName);
        return (val instanceof UUID) ? (UUID) val : null;
    }

    /**
     * Manuell verschobene Unit wird als "persistiert/fixiert" markiert.
     */
    private void setManualPersistFlagIfPresent(LearningUnit unit) {
        tryInvokeBooleanSetter(unit, METHOD_SET_PERSISTED, true);
        tryInvokeBooleanSetter(unit, METHOD_SET_MANUAL_OVERRIDE, true);
        tryInvokeBooleanSetter(unit, METHOD_SET_MANUALLY_MOVED, true);
        tryInvokeBooleanSetter(unit, METHOD_SET_LOCKED, true);
        tryInvokeBooleanSetter(unit, METHOD_SET_FIXED, true);
    }

    private void tryInvokeBooleanSetter(Object target, String methodName, boolean value) {
        try {
            target.getClass().getMethod(methodName, boolean.class).invoke(target, value);
        } catch (Exception ignored) {
            // ok: Feld existiert evtl. noch nicht
        }
    }

    // -------------------------------------------------------------------------
    // Mapping: Domain -> DTO (LearningPlanDTO)
    // -------------------------------------------------------------------------

    private LearningPlanDTO mapToLearningPlanDTO(LearningPlan plan) {
        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setId(plan.getPlanId());
        dto.setValidFrom(plan.getWeekStart());
        dto.setValidUntil(plan.getWeekEnd());
        dto.setUnits(mapUnits(plan));

        User user = extractUserIfPresent(plan);
        if (user != null) {
            dto.setAvailableSlots(mapFreeTimesForWeek(user, plan.getWeekStart(), plan.getWeekEnd()));
        } else {
            dto.setAvailableSlots(Collections.emptyList());
        }

        return dto;
    }

    private User extractUserIfPresent(LearningPlan plan) {
        Object userObj = tryInvokeGetter(plan, METHOD_GET_USER);
        return (userObj instanceof User u) ? u : null;
    }

    private List<UnitDTO> mapUnits(LearningPlan plan) {
        List<UnitDTO> result = new ArrayList<>();

        for (LearningUnit unit : safeList(plan.getUnits())) {
            if (unit == null || unit.getStartTime() == null || unit.getEndTime() == null) {
                continue;
            }

            Task task = unit.getTask();

            String title = task != null ? task.getTitle() : null;
            String description = null;
            String color = null;

            if (task != null && task.getModule() != null) {
                description = firstNonBlank(task.getModule().getDescription(), task.getModule().getTitle());
                color = task.getModule().getColorHexCode();
            }

            UnitDTO ud = new UnitDTO();
            ud.setTitle(title);
            ud.setDescription(description);
            ud.setColor(color);
            ud.setDate(unit.getStartTime().toLocalDate());
            ud.setStart(unit.getStartTime().toLocalTime());
            ud.setEnd(unit.getEndTime().toLocalTime());

            result.add(ud);
        }

        return result;
    }

    private List<FreeTimeDTO> mapFreeTimesForWeek(User user, LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart == null || weekEnd == null) {
            return Collections.emptyList();
        }

        List<FreeTimeDTO> dtos = new ArrayList<>();

        for (FreeTime ft : safeList(user.getFreeTimes())) {
            if (ft == null || ft.getStartTime() == null || ft.getEndTime() == null || !ft.isValidTimeRange()) {
                continue;
            }

            Integer dayIndex = resolveDayIndex(ft, weekStart, weekEnd);
            if (dayIndex == null) {
                continue;
            }

            boolean weekly = ft.getRecurrenceType() == RecurrenceType.WEEKLY;
            LocalDate date = weekStart.plusDays(dayIndex);

            dtos.add(new FreeTimeDTO(
                    ft.getTitle(),
                    date,
                    ft.getStartTime(),
                    ft.getEndTime(),
                    weekly
            ));
        }

        return dtos;
    }

    private Integer resolveDayIndex(FreeTime ft, LocalDate weekStart, LocalDate weekEnd) {
        RecurrenceType type = ft.getRecurrenceType();

        if (type == RecurrenceType.WEEKLY && ft instanceof RecurringFreeTime weekly) {
            return weekly.getDayOfWeek().getValue() - DAY_OF_WEEK_VALUE_MONDAY;
        }

        if (type == RecurrenceType.ONCE && ft instanceof SingleFreeTime once) {
            LocalDate date = once.getDate();
            if (date != null && !date.isBefore(weekStart) && !date.isAfter(weekEnd)) {
                return (int) ChronoUnit.DAYS.between(weekStart, date);
            }
        }

        return null;
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }

    private <T> List<T> safeList(List<T> in) {
        return in == null ? Collections.emptyList() : in;
    }
}

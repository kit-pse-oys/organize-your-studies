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
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    private static final int SLOT_LENGTH_MINUTES = 5;
    private static final int SLOTS_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR / SLOT_LENGTH_MINUTES; // 288 Slots à 5 Minuten
    private static final int DAY_OF_WEEK_VALUE_MONDAY = 1; // Monday=1 ... Sunday=7

    private static final String MSG_DTO_NULL = "dto must not be null";
    private static final String MSG_ACTUAL_DURATION_NEGATIVE = "actualDuration must be >= 0";
    private static final String MSG_START_TIME_NULL_CANNOT_FINISH_EARLY = "Cannot finish unit early: startTime is null";

    private static final String MSG_USER_NOT_FOUND_TEMPLATE = "User not found: %s";
    private static final String MSG_PLAN_NOT_FOUND_TEMPLATE = "LearningPlan not found: %s";
    private static final String MSG_UNIT_NOT_FOUND_TEMPLATE = "LearningUnit not found: %s";

    private static final String MSG_UNIT_NOT_IN_PLAN_TEMPLATE =
            "LearningUnit %s is not part of LearningPlan %s";

    private static final String MSG_START_END_NULL = "start and end must not be null";
    private static final String MSG_END_NOT_AFTER_START = "end must be after start";

    private final UserRepository userRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final LearningUnitRepository learningUnitRepository;

    public LearningUnitService(UserRepository userRepository,
                               LearningPlanRepository learningPlanRepository,
                               LearningUnitRepository learningUnitRepository) {
        this.userRepository = userRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.learningUnitRepository = learningUnitRepository;
    }

    /**
     * Aktualisiert eine bestehende Lerneinheit anhand eines {@link UnitDTO}.
     *
     * <p>Das DTO ist die Kalenderdarstellung im Frontend. Für die Domäne werden daraus
     * vor allem Datum/Start/Ende übernommen. Optional wird auch der Titel der zugrundeliegenden
     * Aufgabe aktualisiert.</p>
     *
     * @param userId die ID des Nutzers
     * @param planId die ID des Lernplans (Woche), in dem die Einheit liegt
     * @param unitId die ID der zu ändernden Lerneinheit
     * @param dto    neue Kalenderdaten
     * @return der aktualisierte Lernplan als DTO
     * @throws IllegalArgumentException wenn User/Plan/Unit nicht existieren oder nicht zusammengehören
     */
    public LearningPlanDTO updateLearningUnit(UUID userId, UUID planId, UUID unitId, UnitDTO dto) {
        Objects.requireNonNull(dto, MSG_DTO_NULL);

        User user = requireUser(userId);
        LearningPlan plan = requirePlanOfUser(user, planId);
        LearningUnit unit = requireUnit(unitId);

        requireUnitInPlan(plan, unit);

        // Zeitfenster aktualisieren (nur wenn Datum + Start + Ende gesetzt sind)
        if (dto.getDate() != null && dto.getStart() != null && dto.getEnd() != null) {
            LocalDateTime start = LocalDateTime.of(dto.getDate(), dto.getStart());
            LocalDateTime end = LocalDateTime.of(dto.getDate(), dto.getEnd());
            validateTimeRange(start, end);
            unit.setStartTime(start);
            unit.setEndTime(end);
        }

        // Titeländerung wirkt fachlich auf die Aufgabe (nicht auf die Unit selbst).
        if (dto.getTitle() != null && unit.getTask() != null && !dto.getTitle().isBlank()) {
            unit.getTask().setTitle(dto.getTitle());
        }

        learningUnitRepository.save(unit);
        return mapToLearningPlanDTO(plan, user);
    }

    /**
     * Verschiebt eine Lerneinheit manuell (z. B. Drag-and-Drop im Kalender).
     *
     * @param userId die ID des Nutzers
     * @param planId die ID des Lernplans
     * @param unitId die ID der zu verschiebenden Einheit
     * @param start  neuer Startzeitpunkt
     * @param end    neues Ende
     * @return der aktualisierte Lernplan als DTO
     * @throws IllegalArgumentException wenn User/Plan/Unit nicht existieren oder nicht zusammengehören
     */
    public LearningPlanDTO moveLearningUnitManually(UUID userId,
                                                    UUID planId,
                                                    UUID unitId,
                                                    LocalDateTime start,
                                                    LocalDateTime end) {
        User user = requireUser(userId);
        LearningPlan plan = requirePlanOfUser(user, planId);
        LearningUnit unit = requireUnit(unitId);

        requireUnitInPlan(plan, unit);
        validateTimeRange(start, end);

        unit.setStartTime(start);
        unit.setEndTime(end);
        learningUnitRepository.save(unit);

        return mapToLearningPlanDTO(plan, user);
    }

    /**
     * Markiert eine Lerneinheit als vorzeitig abgeschlossen und speichert die tatsächliche Dauer.
     *
     * <p>Wenn {@code actualDuration} nicht mitgegeben wird, ermittelt der Server die Dauer
     * aus der Differenz zwischen Startzeitpunkt der Einheit und dem aktuellen Zeitpunkt.</p>
     *
     * @param userId         die ID des Nutzers
     * @param planId         die ID des Lernplans
     * @param unitId         die ID der Einheit
     * @param actualDuration tatsächliche Dauer in Minuten (optional)
     * @return der aktualisierte Lernplan als DTO
     * @throws IllegalArgumentException wenn User/Plan/Unit nicht existieren oder nicht zusammengehören
     */
    public LearningPlanDTO finishUnitEarly(UUID userId,
                                           UUID planId,
                                           UUID unitId,
                                           Integer actualDuration) {
        User user = requireUser(userId);
        LearningPlan plan = requirePlanOfUser(user, planId);
        LearningUnit unit = requireUnit(unitId);

        requireUnitInPlan(plan, unit);

        int minutes;
        if (actualDuration != null) {
            if (actualDuration < 0) {
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
            minutes = (int) Math.max(0, diff);
        }

        unit.markAsCompletedEarly(minutes);
        learningUnitRepository.save(unit);

        return mapToLearningPlanDTO(plan, user);
    }

    // -------------------------------------------------------------------------
    // Laden / Validieren
    // -------------------------------------------------------------------------

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_USER_NOT_FOUND_TEMPLATE, userId)));
    }

    /**
     * Lädt einen Lernplan und stellt sicher, dass er dem Nutzer zugeordnet ist.
     *
     * <p>Primär wird über {@code user.getLearningPlans()} geprüft. Falls die Beziehung in einem
     * frühen Integrationsstand noch nicht befüllt ist, wird ersatzweise direkt über das Repository
     * geladen.</p>
     */
    private LearningPlan requirePlanOfUser(User user, UUID planId) {
        for (LearningPlan p : safeList(user.getLearningPlans())) {
            if (planId.equals(p.getPlanId())) {
                return p;
            }
        }

        return learningPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_PLAN_NOT_FOUND_TEMPLATE, planId)));
    }

    private LearningUnit requireUnit(UUID unitId) {
        return learningUnitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_UNIT_NOT_FOUND_TEMPLATE, unitId)));
    }

    private void requireUnitInPlan(LearningPlan plan, LearningUnit unit) {
        boolean contained = safeList(plan.getUnits()).stream()
                .anyMatch(u -> unit.getUnitId().equals(u.getUnitId()));
        if (!contained) {
            throw new IllegalArgumentException(String.format(
                    MSG_UNIT_NOT_IN_PLAN_TEMPLATE, unit.getUnitId(), plan.getPlanId()));
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(MSG_START_END_NULL);
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(MSG_END_NOT_AFTER_START);
        }
    }

    // -------------------------------------------------------------------------
    // Mapping: Domain -> DTO (LearningPlanDTO)
    // -------------------------------------------------------------------------

    private LearningPlanDTO mapToLearningPlanDTO(LearningPlan plan, User user) {
        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setId(plan.getPlanId());
        dto.setValidFrom(plan.getWeekStart());
        dto.setValidUntil(plan.getWeekEnd());
        dto.setUnits(mapUnits(plan));
        dto.setAvailableSlots(mapFreeTimesForWeek(user, plan.getWeekStart(), plan.getWeekEnd()));
        return dto;
    }

    private List<UnitDTO> mapUnits(LearningPlan plan) {
        List<UnitDTO> result = new ArrayList<>();

        for (LearningUnit unit : safeList(plan.getUnits())) {
            if (unit.getStartTime() == null || unit.getEndTime() == null) {
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

    /**
     * Mappt die Freizeitblöcke eines Nutzers auf Slot-basierte DTOs.
     *
     * <p>Die Slot-Zählung beginnt am Wochenstart (00:00) und nutzt 5-Minuten-Slots.
     * Pro Tag gibt es {@link #SLOTS_PER_DAY} Slots.</p>
     */
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
                    ft.getFreeTimeId(),
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

    private int mapTimeToSlot(LocalTime time) {
        if (time == null) {
            return 0;
        }
        int totalMinutes = time.getHour() * MINUTES_PER_HOUR + time.getMinute();
        return totalMinutes / SLOT_LENGTH_MINUTES;
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


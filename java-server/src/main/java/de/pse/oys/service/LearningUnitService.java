package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service für das Ändern von Lerneinheiten innerhalb eines Lernplans.
 * Lädt Pläne im User-Scope (planId + userId), validiert Zeiträume und Überschneidungen
 * und gibt danach den aktualisierten Plan als DTO zurück.
 */
@Service
@Transactional
public class LearningUnitService {

    private static final String MSG_REQUIRED_FIELDS_MISSING = "Pflichtfelder fehlen.";
    private static final String MSG_TIME_FIELDS_REQUIRED = "Datum, Start und Ende müssen gesetzt sein.";
    private static final String MSG_INVALID_RANGE = "Die Startzeit muss vor der Endzeit liegen.";
    private static final String MSG_UNIT_NOT_FOUND = "LearningUnit existiert nicht.";
    private static final String MSG_ACCESS_DENIED = "Kein Zugriff auf die angefragte Ressource.";
    private static final String MSG_ACTUAL_DURATION_INVALID = "Die tatsächliche Dauer muss >= 0 sein.";
    private static final String MSG_OVERLAP = "Die Einheit überschneidet sich zeitlich mit einer anderen Einheit im Plan.";

    private final LearningUnitRepository learningUnitRepository;
    private final LearningPlanRepository learningPlanRepository;

    /**
     * Erstellt den Service.
     *
     * @param learningPlanRepository Repository für LearningPlans (inkl. Ownership-Query)
     */
    public LearningUnitService(LearningUnitRepository learningUnitRepository, LearningPlanRepository learningPlanRepository) {
        this.learningPlanRepository = learningPlanRepository;
        this.learningUnitRepository = learningUnitRepository;
    }

    /**
     * Aktualisiert das Zeitfenster einer Unit anhand des UnitDTO.
     *
     * @param userId User-Id
     * @param planId Plan-Id
     * @param unitId Unit-Id
     * @param dto    neue Werte
     * @return aktualisierter Plan als DTO
     */
    public LearningPlanDTO updateLearningUnit(UUID userId, UUID planId, UUID unitId, UnitDTO dto) throws ValidationException, AccessDeniedException, ResourceNotFoundException {
        if (userId == null || planId == null || unitId == null || dto == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        LocalDate date = dto.getDate();
        LocalTime start = dto.getStart();
        LocalTime end = dto.getEnd();

        if (date == null || start == null || end == null) {
            throw new ValidationException(MSG_TIME_FIELDS_REQUIRED);
        }

        LearningPlan plan = loadPlanForUserOrThrow(userId, planId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        moveUnitInternal(plan, unit, LocalDateTime.of(date, start), LocalDateTime.of(date, end), true);

        learningPlanRepository.save(plan);
        return toDto(plan);
    }

    /**
     * Verschiebt eine Unit auf einen neuen Zeitraum (z.B. Drag-and-Drop).
     *
     * @param userId User-Id
     * @param planId Plan-Id
     * @param unitId Unit-Id
     * @param start  neuer Start
     * @param end    neues Ende
     * @return aktualisierter Plan als DTO
     */
    public LearningPlanDTO moveLearningUnitManually(UUID userId, UUID planId, UUID unitId,
                                                    LocalDateTime start, LocalDateTime end) {
        if (userId == null || planId == null || unitId == null || start == null || end == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        LearningPlan plan = loadPlanForUserOrThrow(userId, planId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        moveUnitInternal(plan, unit, start, end, true);

        learningPlanRepository.save(plan);
        return toDto(plan);
    }

    /**
     * Markiert eine Unit als vorzeitig abgeschlossen und speichert die tatsächliche Dauer.
     *
     * @param userId         User-Id
     * @param planId         Plan-Id
     * @param unitId         Unit-Id
     * @param actualDuration tatsächliche Minuten (>= 0)
     * @return aktualisierter Plan als DTO
     */
    public LearningPlanDTO finishUnitEarly(UUID userId, UUID planId, UUID unitId, Integer actualDuration) {
        if (userId == null || planId == null || unitId == null || actualDuration == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }
        if (actualDuration < 0) {
            throw new ValidationException(MSG_ACTUAL_DURATION_INVALID);
        }

        LearningPlan plan = loadPlanForUserOrThrow(userId, planId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        unit.markAsCompletedEarly(actualDuration);

        learningPlanRepository.save(plan);
        return toDto(plan);
    }

    public List<WrapperDTO<UnitDTO>> getLearningUnitsByUserId(UUID userId) throws ResourceNotFoundException {
        Objects.requireNonNull(userId, "userId");
        //requireUserExists(userId);
        return learningUnitRepository.findAllByTask_Module_User_UserId(userId).stream()
                .map(unit -> new WrapperDTO<UnitDTO>(unit.getUnitId(), toUnitDto(unit))).toList();
    }

    // -------------------------------------------------------------------------
    // intern
    // -------------------------------------------------------------------------

    /** Lädt den Plan im User-Scope (planId + userId). */
    private LearningPlan loadPlanForUserOrThrow(UUID userId, UUID planId) {
        return learningPlanRepository.findByPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new AccessDeniedException(MSG_ACCESS_DENIED));
    }

    /** Sucht die Unit innerhalb des Plans. */
    private LearningUnit findUnitOrThrow(LearningPlan plan, UUID unitId) {
        return plan.getUnits().stream()
                .filter(u -> u != null && u.getUnitId() != null && unitId.equals(u.getUnitId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MSG_UNIT_NOT_FOUND));
    }

    /** Setzt neue Zeiten und prüft optional Überschneidungen. */
    private void moveUnitInternal(LearningPlan plan, LearningUnit unit,
                                  LocalDateTime start, LocalDateTime end,
                                  boolean checkOverlap) {
        if (!start.isBefore(end)) {
            throw new ValidationException(MSG_INVALID_RANGE);
        }
        if (checkOverlap) {
            assertNoOverlap(plan, unit, start, end);
        }
        unit.setStartTime(start);
        unit.setEndTime(end);
    }

    /** Prüft, ob das neue Zeitfenster mit anderen Units im Plan kollidiert. */
    private void assertNoOverlap(LearningPlan plan, LearningUnit target,
                                 LocalDateTime newStart, LocalDateTime newEnd) {
        for (LearningUnit other : plan.getUnits()) {
            if (other == null || other.getUnitId() == null) {
                continue;
            }
            if (other.getUnitId().equals(target.getUnitId())) {
                continue;
            }

            boolean overlap = newStart.isBefore(other.getEndTime()) && newEnd.isAfter(other.getStartTime());
            if (overlap) {
                throw new ValidationException(MSG_OVERLAP);
            }
        }
    }

    /** Wandelt einen Plan in das Response-DTO um. */
    private LearningPlanDTO toDto(LearningPlan plan) {
        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setId(plan.getPlanId());
        dto.setValidFrom(plan.getWeekStart());
        dto.setValidUntil(plan.getWeekEnd());
        dto.setUnits(plan.getUnits().stream().filter(Objects::nonNull).map(this::mapUnit).toList());
        dto.setAvailableSlots(mapFreeTimes(plan.getFreeTimes()));
        return dto;
    }

    /** Erstellt ein UnitDTO aus einer LearningUnit. */
    private UnitDTO mapUnit(LearningUnit unit) {
        UnitDTO dto = new UnitDTO();

        Task task = unit.getTask();
        if (task != null) {
            dto.setTitle(task.getTitle());

            Module module = task.getModule();
            if (module != null) {
                dto.setDescription(module.getDescription());
                dto.setColor(module.getColorHexCode());
            }
        }

        LocalDateTime startTime = unit.getStartTime();
        if (startTime != null) {
            dto.setDate(startTime.toLocalDate());
            dto.setStart(startTime.toLocalTime());
        }

        LocalDateTime endTime = unit.getEndTime();
        if (endTime != null) {
            dto.setEnd(endTime.toLocalTime());
            if (dto.getDate() == null) {
                dto.setDate(endTime.toLocalDate());
            }
        }

        return dto;
    }

    /** Erstellt FreeTimeDTOs aus den FreeTimes eines Plans. */
    private List<FreeTimeDTO> mapFreeTimes(List<FreeTime> freeTimes) {
        if (freeTimes == null || freeTimes.isEmpty()) {
            return List.of();
        }

        return freeTimes.stream()
                .filter(Objects::nonNull)
                .map(this::mapFreeTime)
                .toList();
    }

    private FreeTimeDTO mapFreeTime(FreeTime ft) {
        boolean weekly = ft.getRecurrenceType() == RecurrenceType.WEEKLY;

        return new FreeTimeDTO(
                ft.getTitle(),
                ft.getRepresentativeDate(),
                ft.getStartTime(),
                ft.getEndTime(),
                weekly
        );
    }

    private UnitDTO toUnitDto(LearningUnit unit) {
        UnitDTO dto = new UnitDTO();

        if (unit.getTask() != null) {
            dto.setTitle(unit.getTask().getTitle());

            // dto.setDescription(unit.getTask().getDescription()); //TODO lässt sich nicht clean transformieren
            // dto.setColor(unit.getTask().getColor());
        }
        if (unit.getStartTime() != null) {
            dto.setDate(unit.getStartTime().toLocalDate());
            dto.setStart(unit.getStartTime().toLocalTime());
        }
        if (unit.getEndTime() != null) {
            if (dto.getDate() == null) {
                dto.setDate(unit.getEndTime().toLocalDate());
            }
            dto.setEnd(unit.getEndTime().toLocalTime());
        }

        return dto;
    }


}

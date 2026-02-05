package de.pse.oys.service;

import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.controller.WrapperDTO;
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
    private static final String MSG_PLAN_NOT_FOUND_FOR_UNIT = "Kein passender Lernplan für diese Einheit gefunden.";

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
     * @param unitId Unit-Id
     * @return aktualisierter Plan als DTO
     */
    public UnitDTO moveLearningUnitAutomatically(UUID userId, UUID unitId) throws ValidationException, AccessDeniedException, ResourceNotFoundException {
        if (userId == null || unitId == null ) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        LearningPlan plan = findPlanByUnitAndUser(userId, unitId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        LocalDateTime unitStartTime = unit.getStartTime();
        LocalDateTime unitEndTime = unit.getEndTime();

        LocalDate date = unitStartTime.toLocalDate();
        LocalTime start = unitStartTime.toLocalTime();
        LocalTime end = unitEndTime.toLocalTime();

        if (date == null || start == null || end == null) {
            throw new ValidationException(MSG_TIME_FIELDS_REQUIRED);
        }
        moveUnitInternal(plan, unit, LocalDateTime.of(date, start), LocalDateTime.of(date, end));

        learningPlanRepository.save(plan);
        return mapUnit(unit);
    }

    /**
     * Verschiebt eine Unit auf einen neuen Zeitraum (z.B. Drag-and-Drop).
     *
     * @param userId User-Id
     * @param unitId Unit-Id
     * @param start  neuer Start
     */
    public void moveLearningUnitManually(UUID userId, UUID unitId, LocalDateTime start) {
        if (userId == null || unitId == null || start == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        LearningPlan plan = findPlanByUnitAndUser(userId, unitId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        long durationMinutes = java.time.Duration.between(unit.getStartTime(), unit.getEndTime()).toMinutes();
        LocalDateTime end = start.plusMinutes(durationMinutes);
        moveUnitInternal(plan, unit, start, end);

        learningPlanRepository.save(plan);
    }

    /**
     * Markiert eine Unit als vorzeitig abgeschlossen und speichert die tatsächliche Dauer.
     *
     * @param userId         User-Id
     * @param unitId         Unit-Id
     * @param actualDuration tatsächliche Minuten (>= 0)
     */
    public void finishUnitEarly(UUID userId, UUID unitId, Integer actualDuration) {
        if (userId == null || unitId == null || actualDuration == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }
        if (actualDuration < 0) {
            throw new ValidationException(MSG_ACTUAL_DURATION_INVALID);
        }

        LearningPlan plan = findPlanByUnitAndUser(userId, unitId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        unit.markAsCompletedEarly(actualDuration);

        learningPlanRepository.save(plan);
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
        return learningPlanRepository.findByIdAndUserId(planId, userId)
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
                                  LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new ValidationException(MSG_INVALID_RANGE);
        }
        assertNoOverlap(plan, unit, start, end);
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


    /** Erstellt ein UnitDTO aus einer LearningUnit. */
    private UnitDTO mapUnit(LearningUnit unit) {
        UnitDTO dto = new UnitDTO();

        Task task = unit.getTask();
        if (task != null) {
            dto.setTitle(task.getTitle());
            dto.setTask(task);

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

    /**
     * Hilfsmethode: Findet den Plan, der eine bestimmte Unit enthält und dem User gehört.
     * Verhindert, dass User Units fremder Pläne manipulieren.
     */
    private LearningPlan findPlanByUnitAndUser(UUID userId, UUID unitId) {
        // Wir suchen in allen Plänen des Users nach der entsprechenden Unit
        return learningPlanRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .filter(p -> p.getUnits().stream().anyMatch(u -> u.getUnitId().equals(unitId)))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PLAN_NOT_FOUND_FOR_UNIT));
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

package de.pse.oys.service;

import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private static final String MSG_INVALID_RANGE = "Die Startzeit muss vor der Endzeit liegen.";
    private static final String MSG_UNIT_NOT_FOUND = "LearningUnit existiert nicht.";
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

    /**
     * Holt alle Units eines Users.
     *
     * @param userId User-Id
     * @return Liste aller Units des Users als DTOs
     */
    public List<WrapperDTO<UnitDTO>> getLearningUnitsByUserId(UUID userId) throws ResourceNotFoundException {
        Objects.requireNonNull(userId, "userId");
        //requireUserExists(userId);
        return learningUnitRepository.findAllByTask_Module_User_UserId(userId).stream()
                .map(unit -> new WrapperDTO<>(unit.getUnitId(), unit.toDTO())).toList();
    }

    // -------------------------------------------------------------------------
    // intern
    // -------------------------------------------------------------------------


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

    /**
     * Hilfsmethode: Findet den Plan, der eine bestimmte Unit enthält und dem User gehört.
     * Verhindert, dass User Units fremder Pläne manipulieren.
     */
    private LearningPlan findPlanByUnitAndUser(UUID userId, UUID unitId) {
        // Wir suchen in allen Plänen des Users nach der entsprechenden Unit
        return learningPlanRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .filter(p -> p.getUnits().stream().anyMatch(u -> u.getUnitId().equals(unitId)))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PLAN_NOT_FOUND_FOR_UNIT));
    }
}

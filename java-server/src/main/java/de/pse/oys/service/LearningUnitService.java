package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.TaskRepository;
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
import java.util.stream.Collectors;

/**
 * LearningUnitService kapselt die Geschäftslogik für Änderungen an Lerneinheiten innerhalb eines Lernplans.
 * <p>
 * Kernaufgaben:
 * <ul>
 *   <li>Ownership/Scope: Laden des Plans ausschließlich im User-Scope (planid + userid).</li>
 *   <li>Validierung: Zeiträume müssen logisch sein (start < end) und dürfen keine Überschneidungen erzeugen.</li>
 *   <li>Persistierung: Änderungen erfolgen am Plan/Units und werden über das LearningPlanRepository gespeichert.</li>
 *   <li>Response: Gibt den aktualisierten Gesamtplan als LearningPlanDTO zurück.</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
public class LearningUnitService {

    private static final String MSG_REQUIRED_FIELDS_MISSING = "Pflichtfelder fehlen.";
    private static final String MSG_INVALID_RANGE = "Die Startzeit muss vor der Endzeit liegen.";
    private static final String MSG_PLAN_NOT_FOUND = "LearningPlan existiert nicht.";
    private static final String MSG_UNIT_NOT_FOUND = "LearningUnit existiert nicht.";
    private static final String MSG_ACCESS_DENIED = "Kein Zugriff auf die angefragte Ressource.";
    private static final String MSG_ACTUAL_DURATION_INVALID = "Die tatsächliche Dauer muss >= 0 sein.";
    private static final String MSG_OVERLAP = "Die Einheit überschneidet sich zeitlich mit einer anderen Einheit im Plan.";

    private final LearningPlanRepository learningPlanRepository;
    private final TaskRepository taskRepository;

    /**
     * Erzeugt den Service.
     *
     * @param learningPlanRepository Repository für LearningPlan-Zugriffe (inkl. Ownership-Scoping)
     * @param taskRepository         Repository für Tasks (derzeit nicht zwingend genutzt; reserviert für Metadaten-Mapping)
     */
    public LearningUnitService(LearningPlanRepository learningPlanRepository,
                               TaskRepository taskRepository) {
        this.learningPlanRepository = Objects.requireNonNull(learningPlanRepository);
        this.taskRepository = Objects.requireNonNull(taskRepository);
    }

    /**
     * Aktualisiert eine spezifische Lerneinheit innerhalb eines Lernplans.
     * <p>
     * Aktuell werden zuverlässig die Zeitdaten synchronisiert (date/start/end).
     * Weitere Metadaten aus {@link UnitDTO} (title/description/color) werden NICHT persistiert,
     * da diese in {@link LearningUnit} nicht modelliert sind.
     * </p>
     *
     * @param userId ID des Users (Scope/Ownership)
     * @param planId ID des Lernplans
     * @param unitId ID der Lerneinheit
     * @param dto    neue Werte (insb. date/start/end)
     * @return aktualisierter Lernplan zur Client-Synchronisation
     */
    public LearningPlanDTO updateLearningUnit(UUID userId, UUID planId, UUID unitId, UnitDTO dto) {
        if (userId == null || planId == null || unitId == null || dto == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        LearningPlan plan = loadPlanForUserOrThrow(userId, planId);
        LearningUnit unit = findUnitOrThrow(plan, unitId);

        // Metadaten wie title/description/color existieren nicht in LearningUnit
        // TODO: Klären, ob diese Daten aus Task kommen und hier sauber gemappt/persistiert werden sollen.

        LocalDate date = dto.getDate();
        LocalTime start = dto.getStart();
        LocalTime end = dto.getEnd();

        if (date != null && start != null && end != null) {
            LocalDateTime newStart = LocalDateTime.of(date, start);
            LocalDateTime newEnd = LocalDateTime.of(date, end);
            moveUnitInternal(plan, unit, newStart, newEnd, true);
        }

        learningPlanRepository.save(plan);
        return toDto(plan);
    }

    /**
     * Verschiebt eine Lerneinheit manuell auf einen neuen Zeitraum (z.B. Drag-and-Drop).
     * <p>
     * Zusätzlich erwähnt das Entwurfsheft ein Persistenz-Flag zum Schutz vor Solver-Überschreibungen.
     * </p>
     *
     * @param userId ID des Users (Scope/Ownership)
     * @param planId ID des Lernplans
     * @param unitId ID der Lerneinheit
     * @param start  neuer Startzeitpunkt
     * @param end    neuer Endzeitpunkt
     * @return aktualisierter Lernplan zur Client-Synchronisation
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
     * Markiert eine Lerneinheit vorzeitig als abgeschlossen und speichert die tatsächliche Dauer.
     *
     * @param userId         ID des Users (Scope/Ownership)
     * @param planId         ID des Lernplans
     * @param unitId         ID der Lerneinheit
     * @param actualDuration tatsächlich aufgewendete Zeit in Minuten (>= 0)
     * @return aktualisierter Lernplan zur Client-Synchronisation
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

    // -------------------------------------------------------------------------
    // internals
    // -------------------------------------------------------------------------

    /**
     * Lädt den Lernplan im User-Scope (Ownership inklusive).
     * <p>
     * Wenn kein Plan zurückkommt, ist entweder der Plan nicht vorhanden ODER gehört nicht dem User.
     * Damit keine Informationen geleakt werden, wird in beiden Fällen AccessDenied geworfen.
     * </p>
     *
     * @param userId User-Scope
     * @param planId Plan-ID
     * @return LearningPlan im User-Scope
     */
    private LearningPlan loadPlanForUserOrThrow(UUID userId, UUID planId) {
        return learningPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new AccessDeniedException(MSG_ACCESS_DENIED));
    }

    /**
     * Findet eine Lerneinheit innerhalb des Plans.
     *
     * @param plan   Lernplan
     * @param unitId ID der Einheit
     * @return gefundene Einheit
     */
    private LearningUnit findUnitOrThrow(LearningPlan plan, UUID unitId) {
        List<LearningUnit> units = plan.getUnits();
        if (units == null || units.isEmpty()) {
            throw new ResourceNotFoundException(MSG_UNIT_NOT_FOUND);
        }

        return units.stream()
                .filter(u -> u != null && unitId.equals(u.getUnitId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MSG_UNIT_NOT_FOUND));
    }

    /**
     * Setzt neue Zeiten für eine Unit, validiert den Zeitraum und optional Überschneidungen.
     *
     * @param plan        Lernplan (für Overlap-Check)
     * @param unit        zu ändernde Unit
     * @param start       neuer Start
     * @param end         neues Ende
     * @param checkOverlap true, wenn Überschneidungen geprüft werden sollen
     */
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

    /**
     * Prüft, ob das neue Zeitfenster einer Unit mit anderen Units im Plan kollidiert.
     *
     * @param plan     Lernplan
     * @param target   Ziel-Unit (wird bei der Prüfung ignoriert)
     * @param newStart neuer Start
     * @param newEnd   neues Ende
     */
    private void assertNoOverlap(LearningPlan plan, LearningUnit target,
                                 LocalDateTime newStart, LocalDateTime newEnd) {
        List<LearningUnit> units = plan.getUnits();
        if (units == null || units.isEmpty()) {
            return;
        }

        for (LearningUnit other : units) {
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
     * Mappt einen {@link LearningPlan} auf das Response-DTO {@link LearningPlanDTO}.
     *
     * @param plan Lernplan
     * @return DTO für das Frontend
     */
    private LearningPlanDTO toDto(LearningPlan plan) {
        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setId(plan.getPlanId());
        dto.setValidFrom(plan.getWeekStart());
        dto.setValidUntil(plan.getWeekEnd());
        dto.setUnits(mapUnits(plan.getUnits()));
        dto.setAvailableSlots(mapFreeTimes(plan.getFreeTimes()));
        return dto;
    }

    /**
     * Mappt die Units des Plans auf {@link UnitDTO}.
     * <p>
     * Aktuell werden date/start/end sauber gesetzt.
     * </p>
     *
     * @param units Liste von LearningUnits
     * @return Liste von UnitDTOs (nie null)
     */
    private List<UnitDTO> mapUnits(List<LearningUnit> units) {
        if (units == null) {
            return List.of();
        }

        return units.stream()
                .filter(Objects::nonNull)
                .map(this::mapUnit)
                .collect(Collectors.toList());
    }

    /**
     * Mappt eine einzelne {@link LearningUnit} auf {@link UnitDTO}.
     *
     * @param unit LearningUnit
     * @return UnitDTO
     */
    private UnitDTO mapUnit(LearningUnit unit) {
        UnitDTO dto = new UnitDTO();

        // TODO: title/description/color:
        // - vermutlich aus unit.getTask() ableitbar (z.B. task.title, task.description, module.color, ...)
        // - erst umsetzen, wenn klar ist, welche Felder existieren und welche Quelle "kanonisch" ist

        if (unit.getStartTime() != null) {
            dto.setDate(unit.getStartTime().toLocalDate());
            dto.setStart(unit.getStartTime().toLocalTime());
        }
        if (unit.getEndTime() != null) {
            dto.setEnd(unit.getEndTime().toLocalTime());
            if (dto.getDate() == null) {
                dto.setDate(unit.getEndTime().toLocalDate());
            }
        }

        return dto;
    }

    /**
     * Mappt FreeTimes auf {@link FreeTimeDTO}.
     * <p>
     * {@link LearningPlan#getFreeTimes()} ist bei euch {@code @Transient} und wird oft dynamisch berechnet.
     * Ohne eure bestehende Mapping-Logik aus dem FreeTime-Kontext wird hier nichts erraten.
     * </p>
     *
     * @param freeTimes Liste von FreeTime (kann null sein)
     * @return Liste von FreeTimeDTOs (nie null)
     */
    private List<FreeTimeDTO> mapFreeTimes(List<FreeTime> freeTimes) {
        if (freeTimes == null || freeTimes.isEmpty()) {
            return List.of();
        }

        // TODO: sobald ihr eine definierte Mapping-Quelle habt (Mapper/Factory/DTO-Constructor),
        //       hier implementieren. Bis dahin lieber leer als falsch.
        return List.of();
    }
}

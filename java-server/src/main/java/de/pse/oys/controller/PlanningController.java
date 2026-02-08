package de.pse.oys.controller;

import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.planning.PlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

/**
 * REST-Controller für die Steuerung der Lernplanung.
 * Ermöglicht es dem Nutzer, die Generierung oder Aktualisierung seines
 * persönlichen Lernplans manuell anzustoßen.
 * @author utgid
 * @version 1.1
 */
@RestController
@RequestMapping("/api/v1/plan")
public class PlanningController extends BaseController {

    private final PlanningService planningService;

    /**
     * Erzeugt eine neue Instanz des PlanningControllers.
     * @param planningService Der Service zur Berechnung des Lernplans.
     */
    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    /**
     * Stößt die Generierung eines neuen Wochenplans für den authentifizierten Nutzer an.
     * @return Status 200 (OK) bei Erfolg.
     */
    @PutMapping
    public ResponseEntity<Void> generateWeeklyPlan() {
        UUID userId = getAuthenticatedUserId();
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        planningService.generateWeeklyPlan(userId, weekStart);
        return ResponseEntity.ok().build();
    }

    /**
     * Verschiebt eine spezifische Lerneinheit auf einen anderen Zeitpunkt.
     * @param wrapperDTO Enthält die ID der Lerneinheit.
     * @return Status 200 (OK), 403 (Forbidden) bei Zugriffsschutz oder 400 (Bad Request).
     */
    @PostMapping("/units/moveAuto")
    public ResponseEntity<WrapperDTO<UnitDTO>> rescheduleUnit(@RequestParam WrapperDTO<Void> wrapperDTO) {
        UUID userId = getAuthenticatedUserId();
        UUID unitId = wrapperDTO.getId();
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        UnitDTO updatedUnit = planningService.rescheduleUnit(userId, weekStart, unitId);
        return ResponseEntity.ok(new WrapperDTO<>(unitId, updatedUnit));
    }
}
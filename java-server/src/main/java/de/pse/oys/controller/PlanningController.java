package de.pse.oys.controller;

import de.pse.oys.service.planning.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/plan")
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
     * @param unitId Die UUID der zu verschiebenden Planungseinheit.
     * @param newStartTime Der neue gewünschte Startzeitpunkt.
     * @return Status 200 (OK), 403 (Forbidden) bei Zugriffsschutz oder 400 (Bad Request).
     */
    @PatchMapping("/{unitId}/reschedule")
    public ResponseEntity<Void> rescheduleUnit( //todo: soll ich das für moveUnit (auto und manuell) nutzen? falls ja anpassen!
            @PathVariable UUID unitId,
            @RequestParam LocalDate newStartTime) {
        try {
            UUID userId = getAuthenticatedUserId();
            planningService.rescheduleUnit(userId, newStartTime, unitId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            // 403 Forbidden, falls die Unit nicht dem anfragenden User gehört
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            // 400 Bad Request bei ungültiger ID oder Zeitformat
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
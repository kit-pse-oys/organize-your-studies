package de.pse.oys.controller;

import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.controller.UnitControlDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.service.LearningUnitService;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST-Controller für die Verwaltung einzelner Lerneinheiten.
 * Ermöglicht manuelle Anpassungen, Verschiebungen und das vorzeitige Beenden von Einheiten.
 */
@RestController
@RequestMapping("/api/v1/plan/units")
public class LearningUnitController extends BaseController { //TODO: richtiges mapping abgleichen

    private final LearningUnitService learningUnitService;

    /**
     * Erzeugt eine neue Instanz des LearningUnitControllers.
     * @param learningUnitService Der Service für die Lerneinheiten-Logik.
     */
    public LearningUnitController(LearningUnitService learningUnitService) {
        this.learningUnitService = learningUnitService;
    }

    /**
     * Aktualisiert eine spezifische Lerneinheit (z.B. Zeitraum).
     * @param planId Die ID des zugehörigen Lernplans.
     * @param unitId Die ID der zu ändernden Einheit.
     * @return Der aktualisierte Gesamtplan als DTO.
     */
    @PatchMapping("/{planId}/{unitId}")
    public ResponseEntity<LearningPlanDTO> moveLearningUnitAutomatically(
            @PathVariable UUID planId,
            @PathVariable UUID unitId) {

        UUID userId = getAuthenticatedUserId();
        LearningPlanDTO updatedPlan = learningUnitService.moveLearningUnitAutomatically(userId, planId, unitId);
        return ResponseEntity.ok(updatedPlan);

    }

    /**
     * Verschiebt eine Lerneinheit manuell auf einen neuen Zeitraum.
     * @param planId Die ID des Lernplans.
     * @param unitId Die ID der Einheit.
     * @param start Der neue Startzeitpunkt.
     * @param end Der neue Endzeitpunkt.
     * @return Der aktualisierte Gesamtplan.
     */
    @PatchMapping("/{planId}/{unitId}/move")
    public ResponseEntity<LearningPlanDTO> moveLearningUnitManually(
            @PathVariable UUID planId,
            @PathVariable UUID unitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            UUID userId = getAuthenticatedUserId();
            LearningPlanDTO updatedPlan = learningUnitService.moveLearningUnitManually(userId, planId, unitId, start, end);
            return ResponseEntity.ok(updatedPlan);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ValidationException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Markiert eine Einheit als vorzeitig beendet und erfasst die tatsächliche Dauer.
     * @param control Die Steuerungsinformationen zur Einheit.
     * @return Der aktualisierte Gesamtplan.
     */
    @PostMapping(params = "finished")
    public ResponseEntity<LearningPlanDTO> finishUnitEarly(@RequestBody UnitControlDTO control) {//TODO: mit richtiger zeitlänge verbessern
        UUID userId = getAuthenticatedUserId();
        learningUnitService.finishUnitEarly(userId, control.getId());
        return ResponseEntity.ok().build();
    }
}
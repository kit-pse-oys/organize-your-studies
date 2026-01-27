package de.pse.oys.controller;

import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.service.LearningUnitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST-Controller für die Verwaltung einzelner Lerneinheiten.
 * Ermöglicht manuelle Anpassungen, Verschiebungen und das vorzeitige Beenden von Einheiten.
 */
@RestController
@RequestMapping("/plan/units")
public class LearningUnitController extends BaseController {

    private final LearningUnitService learningUnitService;

    /**
     * Erzeugt eine neue Instanz des LearningUnitControllers.
     * @param learningUnitService Der Service für die Lerneinheiten-Logik.
     */
    public LearningUnitController(LearningUnitService learningUnitService) {
        this.learningUnitService = learningUnitService;
    }

    /**
     * Aktualisiert eine spezifische Lerneinheit (z.B. Titel oder Beschreibung).
     * @param planId Die ID des zugehörigen Lernplans.
     * @param unitId Die ID der zu ändernden Einheit.
     * @param dto Die neuen Daten der Einheit.
     * @return Der aktualisierte Gesamtplan als DTO.
     */
    @PutMapping("/{unitId}")
    public ResponseEntity<LearningPlanDTO> updateLearningUnit(
            @PathVariable UUID planId,
            @PathVariable UUID unitId,
            @RequestBody UnitDTO dto) {
        UUID userId = getAuthenticatedUserId();
        LearningPlanDTO updatedPlan = learningUnitService.updateLearningUnit(userId, planId, unitId, dto);
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
    @PatchMapping("/{unitId}/move")
    public ResponseEntity<LearningPlanDTO> moveLearningUnitManually(
            @PathVariable UUID planId,
            @PathVariable UUID unitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        UUID userId = getAuthenticatedUserId();
        LearningPlanDTO updatedPlan = learningUnitService.moveLearningUnitManually(userId, planId, unitId, start, end);
        return ResponseEntity.ok(updatedPlan);
    }

    /**
     * Markiert eine Einheit als vorzeitig beendet und erfasst die tatsächliche Dauer.
     * @param planId Die ID des Lernplans.
     * @param unitId Die ID der Einheit.
     * @param actualDuration Die tatsächlich benötigte Zeit in Minuten.
     * @return Der aktualisierte Gesamtplan.
     */
    @PostMapping("/{unitId}/finish")
    public ResponseEntity<LearningPlanDTO> finishUnitEarly(
            @PathVariable UUID planId,
            @PathVariable UUID unitId,
            @RequestParam Integer actualDuration) {
        UUID userId = getAuthenticatedUserId();
        LearningPlanDTO updatedPlan = learningUnitService.finishUnitEarly(userId, planId, unitId, actualDuration);
        return ResponseEntity.ok(updatedPlan);
    }
}
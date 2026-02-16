package de.pse.oys.controller;

import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.controller.UnitControlDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.service.LearningUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Verwaltung einzelner Lerneinheiten.
 * Ermöglicht manuelle Anpassungen, Verschiebungen und das vorzeitige Beenden von Einheiten.
 */
@RestController
@RequestMapping("/api/v1/plan/units")
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
     * Verschiebt eine Lerneinheit manuell auf einen neuen Zeitraum.
     * @param control Die Steuerungsinformationen zur Einheit.
     * @return Der aktualisierte Gesamtplan.
     */
    @PostMapping("/move")
    public ResponseEntity<Void> moveLearningUnitManually(@RequestBody UnitControlDTO control) {
        UUID userId = getAuthenticatedUserId();
        learningUnitService.moveLearningUnitManually(userId, control.getId(), control.getNewTime());
        return ResponseEntity.ok().build();
    }

    /**
     * Markiert eine Einheit als vorzeitig beendet und erfasst die tatsächliche Dauer.
     * @param control Die Steuerungsinformationen zur Einheit.
     * @return Der aktualisierte Gesamtplan.
     */
    @PostMapping("/finished")
    public ResponseEntity<LearningPlanDTO> finishUnitEarly(@RequestBody UnitControlDTO control) {
        UUID userId = getAuthenticatedUserId();
        learningUnitService.finishUnitEarly(userId, control.getId(), control.getActualDuration());
        return ResponseEntity.ok().build();
    }

    /**
     * Holt alle Lerneinheiten für den authentifizierten Benutzer.
     * @return Liste der Lerneinheiten als DTOs.
     */
    @GetMapping
    public ResponseEntity<List<WrapperDTO<UnitDTO>>> getLearningUnitsByUserId() {
        UUID userId = getAuthenticatedUserId();
        List<WrapperDTO<UnitDTO>> response = learningUnitService.getLearningUnitsByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
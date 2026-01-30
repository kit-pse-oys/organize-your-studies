package de.pse.oys.controller;

import de.pse.oys.dto.RatingDTO;
import de.pse.oys.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-Controller für die Bewertung von Lerneinheiten.
 * Verarbeitet Nutzer-Feedback zu abgeschlossenen Einheiten, um künftige
 * Planungen durch den Algorithmus zu optimieren.
 */
@RestController
@RequestMapping("/plan/units/ratings")
public class RatingController extends BaseController {

    private final RatingService ratingService;

    /**
     * Erzeugt eine neue Instanz des RatingControllers.
     * @param ratingService Der Service für die Bewertungslogik.
     */
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Übermittelt eine Bewertung für eine Lerneinheit.
     * Das Feedback wird genutzt, um die Effizienz der Planung zu steigern.
     * @param learningUnitId Die ID der bewerteten Lerneinheit.
     * @param dto Das DTO mit den Bewertungsinformationen (Konzentration, Dauer, Erfolg).
     * @return Eine leere ResponseEntity bei Erfolg.
     */
    @PostMapping("/{learningUnitId}/rate")
    public ResponseEntity<Void> rateUnit(@PathVariable UUID learningUnitId, @RequestBody RatingDTO dto) {
        // Der RatingService verarbeitet die Logik und aktualisiert ggf. die CostMatrix
        try {
            ratingService.submitRating(learningUnitId, dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Markiert eine spezifische Aufgabe als verpasst.
     * Dies triggert im Service die entsprechende Logik zur Anpassung der Planung.
     *
     * @param unitId Die UUID der Aufgabe, die als verpasst markiert werden soll.
     * @return Status 200 (OK) bei Erfolg.
     */
    @PostMapping("/{unitId}/missed")
    public ResponseEntity<Void> markAsMissed(@PathVariable UUID unitId) {
        try {
            ratingService.markAsMissed(unitId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
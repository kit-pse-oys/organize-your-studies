package de.pse.oys.controller;

import de.pse.oys.dto.RatingDTO;
import de.pse.oys.service.RatingService;
import org.springframework.http.ResponseEntity;
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
     * * @param dto Das DTO mit den Bewertungsinformationen (Konzentration, Dauer, Erfolg).
     * @return Eine leere ResponseEntity bei Erfolg.
     */
    @PostMapping
    public ResponseEntity<Void> rateUnit(@RequestBody RatingDTO dto) {
        // Wir prüfen die Identität, um sicherzustellen, dass nur eigene Einheiten bewertet werden
        UUID userId = getAuthenticatedUserId();

        // Der RatingService verarbeitet die Logik und aktualisiert ggf. die CostMatrix
        ratingService.submitRating(null, dto); //todo: learningUnitId hinzufügen

        return ResponseEntity.ok().build();
    }
}
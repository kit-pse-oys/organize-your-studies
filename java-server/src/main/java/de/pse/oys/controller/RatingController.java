package de.pse.oys.controller;

import de.pse.oys.dto.RatingDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Bewertung von Lerneinheiten.
 * Verarbeitet Nutzer-Feedback zu abgeschlossenen Einheiten, um künftige
 * Planungen durch den Algorithmus zu optimieren.
 */
@RestController
@RequestMapping("/api/v1/plan/units/ratings")
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
     *
     * @param wrapperDTO Enthält die ID der Lerneinheit und die Bewertung als RatingDTO.
     * @return Eine leere ResponseEntity bei Erfolg.
     */
    @PostMapping
    public ResponseEntity<Void> rateUnit(@RequestBody WrapperDTO<RatingDTO> wrapperDTO) {
        ratingService.submitRating(wrapperDTO.getId(), wrapperDTO.getData());
        return ResponseEntity.ok().build();
    }

    /**
     * Markiert eine spezifische Aufgabe als verpasst.
     * Dies triggert im Service die entsprechende Logik zur Anpassung der Planung.
     *
     * @param wrapper Enthält die ID der Lerneinheit, die als verpasst markiert werden soll.
     * @return Status 200 (OK) bei Erfolg.
     */
    @PostMapping("/missed")
    public ResponseEntity<Void> markAsMissed(@RequestBody WrapperDTO<Void> wrapper) {
        ratingService.markAsMissed(wrapper.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Ruft alle Bewertungen des aktuell angemeldeten Nutzers ab.
     * @return Eine Liste von WrapperDTOs mit den RatingDTOs der Bewertungen.
     */
    @GetMapping
    public ResponseEntity<List<UUID>> getRateableUnits() {
        UUID userId = getAuthenticatedUserId();
        List<UUID> ratings = ratingService.getRateableUnits(userId);
        return ResponseEntity.ok(ratings);
    }
}
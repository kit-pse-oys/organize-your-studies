package de.pse.oys.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PlanningController – Der PlanningController stellt Endpunkte für die übergeordnete Planungsebene dar.
 * Über diesen Controller wird die Generierung des Lernplans angestoßen.
 *
 * @author uhupo
 * @version 1.0
 */

@RestController
@RequestMapping("/plan")
public class PlanningController {


    /**
     * Generiert einen neuen Lernplan für den angemeldeten Benutzer.
     *
     * @return der neu generierte Lernplan im DTO-Format
     */
    @GetMapping
    public String forceUpdatePlan() {
        return "pong"; //Aktuell Platzhalter nur zum Testen, Rückgabetyp entsprechend falsch
    }
}

package de.pse.oys.controller;

import de.pse.oys.dto.response.LearningPlanDTO;
import de.pse.oys.service.planning.PlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-Controller für die Steuerung der Lernplanung.
 * Ermöglicht es dem Nutzer, die Generierung oder Aktualisierung seines
 * persönlichen Lernplans manuell anzustoßen.
 * @author utgid
 * @version 1.0
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

    //keine forceUpdatePlan-Methode, da diese Methode im Client nicht implementiert wird
}
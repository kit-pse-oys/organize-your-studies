package de.pse.oys.controller;

import de.pse.oys.security.UserPrincipal;
import de.pse.oys.service.planning.PlanningService;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

/**
 * Basis-Controller für gemeinsame Funktionalitäten aller REST-Controller.
 * @author utgid
 * @version 1.0
 */
public abstract class BaseController {

    /**
     * Extrahiert die UUID des aktuell authentifizierten Benutzers.
     * @return Die UUID des Nutzers.
     * @throws ClassCastException falls kein UserPrincipal im Kontext liegt.
     */
    protected UUID getAuthenticatedUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }

    /**
     * Hilfsmethode, um nach Änderungen an einer Lerneinheit oder ähnlichem den Plan neu zu berechnen.
     * @param userId Die UUID des Nutzers, für den der Plan aktualisiert werden soll.
     * @param planningService Der Service, der die Planungslogik enthält.
     */
    protected void updatePlanAfterChange(UUID userId, PlanningService planningService) {
        planningService.generateWeeklyPlan(userId); // null = aktueller Zeitraum
    }
}
package de.pse.oys.controller;

import de.pse.oys.security.UserPrincipal;
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
}
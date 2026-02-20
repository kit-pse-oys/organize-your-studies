package de.pse.oys.controller;

import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.FreeTimeService;
import de.pse.oys.service.planning.PlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST-Controller für die Verwaltung von Freizeiträumen.
 * Ermöglicht das Abrufen, Erstellen und Bearbeiten von Zeitfenstern.
 * @author utgid
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/freeTimes")
public class FreeTimeController extends BaseController {

    private final FreeTimeService freeTimeService;
    private final PlanningService planningService;

    /**
     * Erzeugt eine neue Instanz des FreeTimeControllers.
     * @param freeTimeService Der Service für die Freizeitverwaltung.
     */
    public FreeTimeController(FreeTimeService freeTimeService, PlanningService planningService) {
        this.freeTimeService = freeTimeService;
        this.planningService = planningService;
    }


    /**
     * Ruft alle Freizeiträume ab.
     *
     * @return Eine Liste aller FreeTimeDTOs des authentifizierten Nutzers.
     */
    @GetMapping
    public ResponseEntity<List<WrapperDTO<FreeTimeDTO>>> queryFreeTimes() {
        UUID userId = getAuthenticatedUserId();
        List<WrapperDTO<FreeTimeDTO>> freeTimes = freeTimeService.getFreeTimesByUserId(userId);
        return ResponseEntity.ok(freeTimes);
    }

    /**
     * Erstellt einen neuen Zeitraum für Freizeit.
     * @param dto Die Daten des neuen Zeitfensters.
     * @return Das erstellte DTO oder 400 bei Validierungsfehlern.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createFreeTime(@RequestBody FreeTimeDTO dto) {
        UUID userId = getAuthenticatedUserId();
        UUID created = freeTimeService.createFreeTime(userId, dto);
        updatePlanAfterChange(userId, planningService);
        return ResponseEntity.ok(Map.of("id", created.toString()));
    }

    /**
     * Aktualisiert einen bestehenden Zeitraum.
     * Prüft dabei, ob das Objekt existiert und dem Nutzer gehört.
     *
     * @param wrapperDTO Enthält die ID des zu aktualisierenden Freizeitraums und die neuen Daten als FreeTimeDTO.
     * @return Das geänderte FreeTimeDTO oder ein entsprechender Fehlerstatus.
     */
    @PutMapping
    public ResponseEntity<Void> updateFreeTime(@RequestBody WrapperDTO<FreeTimeDTO> wrapperDTO) {
        FreeTimeDTO dto = wrapperDTO.getData();
        UUID freeTimeId = wrapperDTO.getId();
        UUID userId = getAuthenticatedUserId();
        freeTimeService.updateFreeTime(userId, freeTimeId, dto);
        updatePlanAfterChange(userId, planningService);
        return ResponseEntity.ok().build();

    }

    /**
     * Löscht einen Freizeitraum anhand der im Body übergebenen ID.
     *
     * @param wrapperDTO DTO mit der ID des zu löschenden Freizeitraums.
     * @return 204 bei Erfolg, 403 bei fehlender Berechtigung oder 404.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFreeTime(@RequestBody WrapperDTO<Void> wrapperDTO) {
        UUID userId = getAuthenticatedUserId();
        freeTimeService.deleteFreeTime(userId, wrapperDTO.getId());
        updatePlanAfterChange(userId, planningService);
        return ResponseEntity.noContent().build();
    }
}
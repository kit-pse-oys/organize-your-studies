package de.pse.oys.controller;

import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.service.FreeTimeService;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-Controller für die Verwaltung von Freizeiträumen.
 * Ermöglicht das Abrufen, Erstellen und Bearbeiten von Zeitfenstern.
 * @author utgid
 * @version 1.0
 */
@RestController
@RequestMapping("/freeTimes")
public class FreeTimeController extends BaseController {

    private final FreeTimeService freeTimeService;

    /**
     * Erzeugt eine neue Instanz des FreeTimeControllers.
     * @param freeTimeService Der Service für die Freizeitverwaltung.
     */
    public FreeTimeController(FreeTimeService freeTimeService) {
        this.freeTimeService = freeTimeService;
    }


    /**
     * Erstellt einen neuen Zeitraum für Freizeit.
     * @param dto Die Daten des neuen Zeitfensters.
     * @return Das erstellte DTO oder 400 bei Validierungsfehlern.
     */
    @PostMapping
    public ResponseEntity<UUID> createFreeTime(@RequestBody FreeTimeDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            UUID freeTimeId = freeTimeService.createFreeTime(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(freeTimeId);
        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Aktualisiert einen bestehenden Zeitraum.
     * Prüft dabei, ob das Objekt existiert und dem Nutzer gehört.
     *
     * @param freeTimeId Die ID des zu ändernden Freizeitraums.
     * @param dto Die aktualisierten Daten (muss eine gültige ID enthalten).
     * @return Das geänderte FreeTimeDTO oder ein entsprechender Fehlerstatus.
     */
    @PatchMapping("/{freeTimeId}")
    public ResponseEntity<FreeTimeDTO> updateFreeTime(@PathVariable UUID freeTimeId, @RequestBody FreeTimeDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            FreeTimeDTO updated = freeTimeService.updateFreeTime(userId, freeTimeId, dto);
            return ResponseEntity.ok(updated);
        } catch (AccessDeniedException e) {
            // 403 Forbidden, falls der Nutzer nicht der Besitzer ist
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException | ValidationException e) {
            // 400 Bad Request, falls die ID fehlt oder Validierungsregeln verletzt wurden
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // 500 internal Server Error für unerwartete Probleme
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Löscht einen Freizeitraum anhand der im Body übergebenen ID.
     *
     * @param freeTimeId Die ID des zu löschenden Freizeitraums.
     * @return 204 bei Erfolg, 403 bei fehlender Berechtigung oder 404.
     */
    @DeleteMapping("/{freeTimeId}")
    public ResponseEntity<Void> deleteFreeTime(@PathVariable UUID freeTimeId) {
        try {
            UUID userId = getAuthenticatedUserId();
            freeTimeService.deleteFreeTime(userId, freeTimeId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            // Falls das Objekt nicht dem User gehört
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException | ValidationException e) {
            // Falls die ID nicht existiert
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
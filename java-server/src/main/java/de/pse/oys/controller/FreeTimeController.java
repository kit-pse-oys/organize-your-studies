package de.pse.oys.controller;

import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.service.FreeTimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * Ruft alle registrierten Freizeiträume des Nutzers ab.
     * @return Liste der Freizeiträume oder 500 bei Fehlern.
     */
    @GetMapping
    public ResponseEntity<List<FreeTimeDTO>> getFreeTimes() {
        try {
            UUID userId = getAuthenticatedUserId();
            List<FreeTimeDTO> freeTimes = freeTimeService.getFreeTimesByUserId(userId);
            return ResponseEntity.ok(freeTimes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Erstellt einen neuen Zeitraum für Freizeit.
     * @param dto Die Daten des neuen Zeitfensters.
     * @return Das erstellte DTO oder 400 bei Validierungsfehlern.
     */
    @PostMapping
    public ResponseEntity<FreeTimeDTO> createFreeTime(@RequestBody FreeTimeDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            FreeTimeDTO created = freeTimeService.createFreeTime(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Aktualisiert einen bestehenden Zeitraum.
     * Prüft dabei, ob das Objekt existiert und dem Nutzer gehört.
     *
     * @param dto Die aktualisierten Daten (muss eine gültige ID enthalten).
     * @return Das geänderte FreeTimeDTO oder ein entsprechender Fehlerstatus.
     */
    @PutMapping
    public ResponseEntity<FreeTimeDTO> updateFreeTime(@RequestBody FreeTimeDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            FreeTimeDTO updated = freeTimeService.updateFreeTime(userId, dto);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            // 403 Forbidden, falls der Nutzer nicht der Besitzer ist
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            // 400 Bad Request, falls die ID fehlt oder Validierungsregeln verletzt wurden
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // 500 Internal Server Error für unerwartete Probleme
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Löscht einen Freizeitraum anhand der im Body übergebenen ID.
     * @param dto Enthält die ID des zu löschenden Objekts.
     * @return 204 bei Erfolg, 403 bei fehlender Berechtigung oder 404.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFreeTime(@RequestBody FreeTimeDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            freeTimeService.deleteFreeTime(userId, dto.getId());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            // Falls das Objekt nicht dem User gehört
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            // Falls die ID nicht existiert
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
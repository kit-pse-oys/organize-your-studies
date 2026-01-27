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
     * @return Eine Liste von FreeTimeDTOs.
     */
    @GetMapping
    public ResponseEntity<List<FreeTimeDTO>> getFreeTimes() {
        UUID userId = getAuthenticatedUserId();
        List<FreeTimeDTO> freeTimes = freeTimeService.getFreeTimesByUserId(userId);
        return ResponseEntity.ok(freeTimes);
    }

    /**
     * Erstellt einen neuen Zeitraum für Freizeit.
     * @param dto Die Daten des neuen Zeitfensters.
     * @return Das erstellte FreeTimeDTO.
     */
    @PostMapping
    public ResponseEntity<FreeTimeDTO> createFreeTime(@RequestBody FreeTimeDTO dto) {
        UUID userId = getAuthenticatedUserId();
        FreeTimeDTO created = freeTimeService.createFreeTime(userId, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Aktualisiert einen bestehenden Zeitraum.
     * @param dto Die aktualisierten Daten.
     * @return Das geänderte FreeTimeDTO.
     */
    @PutMapping
    public ResponseEntity<FreeTimeDTO> updateFreeTime(@RequestBody FreeTimeDTO dto) {
        UUID userId = getAuthenticatedUserId();
        FreeTimeDTO updated = freeTimeService.updateFreeTime(userId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Löscht einen Zeitraum aus der Planung des Nutzers.
     * @param dto Das DTO, welches den zu löschenden Zeitraum identifiziert.
     * @return Status 204 (No Content).
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFreeTime(@RequestBody FreeTimeDTO dto) {
        UUID userId = getAuthenticatedUserId();
        freeTimeService.deleteFreeTime(userId, dto.getId());
        return ResponseEntity.noContent().build();
    }
}
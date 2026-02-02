package de.pse.oys.controller;

import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.controller.IdDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.FreeTimeService;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.http.HttpStatus;
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

    /**
     * Erzeugt eine neue Instanz des FreeTimeControllers.
     * @param freeTimeService Der Service für die Freizeitverwaltung.
     */
    public FreeTimeController(FreeTimeService freeTimeService) {
        this.freeTimeService = freeTimeService;
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
        return ResponseEntity.ok(freeTimes); //todo rückgabe klasse mit id und data (date= dto)
    }

    /**
     * Erstellt einen neuen Zeitraum für Freizeit.
     * @param dto Die Daten des neuen Zeitfensters.
     * @return Das erstellte DTO oder 400 bei Validierungsfehlern.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createFreeTime(@RequestBody FreeTimeDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            UUID created = freeTimeService.createFreeTime(userId, dto);
            return ResponseEntity.ok(Map.of("id", created.toString())); //todo: return created id wird vom service bereit gestellt
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
     * @param wrapperDTO@return Das geänderte FreeTimeDTO oder ein entsprechender Fehlerstatus.
     */
    @PutMapping
    public ResponseEntity<Void> updateFreeTime(@RequestBody WrapperDTO<FreeTimeDTO> wrapperDTO) {
        FreeTimeDTO dto = wrapperDTO.getData();
        UUID freeTimeId = wrapperDTO.getId();
        try {
            UUID userId = getAuthenticatedUserId();
            freeTimeService.updateFreeTime(userId, freeTimeId, dto);
            return ResponseEntity.ok().build();
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
     * @param idDto DTO mit der ID des zu löschenden Freizeitraums.
     * @return 204 bei Erfolg, 403 bei fehlender Berechtigung oder 404.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFreeTime(@RequestBody IdDTO idDto) {
        try {
            UUID userId = getAuthenticatedUserId();
            freeTimeService.deleteFreeTime(userId, idDto.asUuid());
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
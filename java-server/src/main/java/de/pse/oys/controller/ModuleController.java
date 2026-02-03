package de.pse.oys.controller;

import de.pse.oys.dto.ModuleDTO;
import de.pse.oys.service.ModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


/**
 * REST-Controller für die Verwaltung von Modulen.
 * Bietet Endpunkte zum Abrufen, Erstellen, Aktualisieren und Löschen von Modulen.
 */
@RestController
@RequestMapping("/modules")
public class ModuleController extends BaseController {

    private final ModuleService moduleService;

    /**
     * Erzeugt eine neue Instanz des ModuleControllers.
     * @param moduleService Der Service für die Modulverwaltung.
     */
    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    /**
     * Erstellt ein neues Modul.
     * @param dto Die Daten des zu erstellenden Moduls.
     * @return Eine ResponseEntity mit dem erstellten Modul (Status 201).
     */
    @PostMapping("/create")
    public ResponseEntity<UUID> createModule(@RequestBody ModuleDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            UUID moduleId = moduleService.createModule(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(moduleId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Aktualisiert ein bestehendes Modul.
     * @param dto Die aktualisierten Moduldaten.
     * @return Eine ResponseEntity mit dem geänderten Modul (Status 200).
     */
    @PutMapping("/update")
    public ResponseEntity<ModuleDTO> updateModule(@RequestBody ModuleDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            ModuleDTO updated = moduleService.updateModule(userId, dto);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            // Nutzer versucht ein Modul zu ändern, das ihm nicht gehört
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            // ID fehlt oder Titel ist leer
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Löscht ein Modul aus dem System.
     * @param dto Das DTO, welches das zu löschende Modul identifiziert.
     * @return 200 bei Erfolg, 403 bei fehlender Berechtigung.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteModule(@RequestBody ModuleDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            moduleService.deleteModule(userId, dto.getId());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
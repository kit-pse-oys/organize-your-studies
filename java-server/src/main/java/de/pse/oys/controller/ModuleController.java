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
    @PostMapping
    public ResponseEntity<ModuleDTO> createModule(@RequestBody ModuleDTO dto) {
        UUID userId = getAuthenticatedUserId();
        ModuleDTO created = moduleService.createModule(userId, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Aktualisiert ein bestehendes Modul.
     * @param dto Die aktualisierten Moduldaten.
     * @return Eine ResponseEntity mit dem geänderten Modul (Status 200).
     */
    @PutMapping
    public ResponseEntity<ModuleDTO> updateModule(@RequestBody ModuleDTO dto) {
        UUID userId = getAuthenticatedUserId();
        ModuleDTO updated = moduleService.updateModule(userId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Löscht ein Modul aus dem System.
     * @param dto Das DTO, welches das zu löschende Modul identifiziert.
     * @return Eine leere ResponseEntity (Status 204).
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteModule(@RequestBody ModuleDTO dto) {
        UUID userId = getAuthenticatedUserId();
        moduleService.deleteModule(userId, null); //todo irgendwie an die UUID des moduls rankommen
        return ResponseEntity.noContent().build();
    }
}
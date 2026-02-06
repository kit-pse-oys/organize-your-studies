package de.pse.oys.controller;

import de.pse.oys.dto.ModuleDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.ModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * REST-Controller für die Verwaltung von Modulen.
 * Bietet Endpunkte zum Abrufen, Erstellen, Aktualisieren und Löschen von Modulen.
 */
@RestController
@RequestMapping("/api/v1/modules")
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
    public ResponseEntity<Map<String, UUID>> createModule(@RequestBody ModuleDTO dto) {
        UUID userId = getAuthenticatedUserId();
        UUID moduleId = moduleService.createModule(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", moduleId));
    }


    /**
     * Aktualisiert ein bestehendes Modul.
     * @param wrapper Die aktualisierten Moduldaten.
     * @return Eine ResponseEntity mit dem geänderten Modul (Status 200).
     */
    @PutMapping
    public ResponseEntity<Void> updateModule(@RequestBody WrapperDTO<ModuleDTO> wrapper) {
        UUID userId = getAuthenticatedUserId();
        ModuleDTO data = wrapper.getData();
        data.setId(wrapper.getId());
        moduleService.updateModule(userId, data);
        return ResponseEntity.ok().build();
    }

    /**
     * Löscht ein Modul aus dem System.
     * @param dto Das DTO, welches das zu löschende Modul identifiziert.
     * @return 200 bei Erfolg.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteModule(@RequestBody ModuleDTO dto) {
        UUID userId = getAuthenticatedUserId();
        moduleService.deleteModule(userId, dto.getId());
        return ResponseEntity.ok().build();
    }


    /**
    * Gibt alle Module zurück, die dem authentifizierten Nutzer zugeordnet sind.
    * @return Eine Liste von Modulen (Status 200).
    */
    @GetMapping
    public ResponseEntity<List<WrapperDTO<ModuleDTO>>> getAllModules() {
        UUID userId = getAuthenticatedUserId();
        List<WrapperDTO<ModuleDTO>> modules = moduleService.getModulesByUserId(userId);
        return ResponseEntity.ok(modules);
    }
}
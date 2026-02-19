package de.pse.oys.controller;

import de.pse.oys.dto.TaskDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.TaskService;
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
 * REST-Controller für die Aufgabenverwaltung.
 * Nutzt den BaseController, um auf die Identität des Nutzers zuzugreifen.
 * @author utgid
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController extends BaseController {

    private final TaskService taskService;
    private final PlanningService planningService;

    /**
     * Erzeugt eine neue Instanz des TaskControllers.
     * @param taskService Der Service für die Aufgabenlogik.
     */
    public TaskController(TaskService taskService, PlanningService planningService) {
        this.planningService = planningService;
        this.taskService = taskService;
    }

    /**
     * Ruft alle Aufgaben des authentifizierten Nutzers ab.
     * @return Liste der Aufgaben als DTOs.
     */
    @GetMapping
    public ResponseEntity<List<WrapperDTO<TaskDTO>>> getTasks() {
        UUID userId = getAuthenticatedUserId();
        List<WrapperDTO<TaskDTO>> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);

    }

    /**
     * Erstellt eine neue Aufgabe für den Nutzer.
     * @param dto Die Daten der neuen Aufgabe.
     * @return Das erstellte TaskDTO.
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createTask(@RequestBody TaskDTO dto) {
        UUID userId = getAuthenticatedUserId();
        UUID taskId = taskService.createTask(userId, dto);
        updatePlanAfterChange(userId, planningService);
        return ResponseEntity.ok(Map.of("id", taskId));
    }

    /**
     * Aktualisiert eine bestehende Aufgabe.
     * @param wrapper Enthält die ID des Tasks und die neuen Daten als TaskDTO.
     * @return Status 200 (OK) bei Erfolg.
     */
    @PutMapping
    public ResponseEntity<Map<String, UUID>> updateTask(@RequestBody WrapperDTO<TaskDTO> wrapper) {
        UUID userId = getAuthenticatedUserId();
        UUID taskId = taskService.updateTask(userId, wrapper.getId(), wrapper.getData());
        updatePlanAfterChange(userId, planningService);
        return ResponseEntity.ok(Map.of("id", taskId));

    }

    /**
     * Löscht eine Aufgabe.
     *
     * @param wrapper Enthält die ID der zu löschenden Aufgabe.
     * @return No Content Status.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteTask(@RequestBody WrapperDTO<Void> wrapper) {
        UUID userId = getAuthenticatedUserId();
        taskService.deleteTask(userId, wrapper.getId());
        updatePlanAfterChange(userId, planningService);
        return ResponseEntity.noContent().build();
    }
}
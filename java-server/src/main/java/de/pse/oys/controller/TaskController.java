package de.pse.oys.controller;

import de.pse.oys.dto.TaskDTO;
import de.pse.oys.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Aufgabenverwaltung.
 * Nutzt den BaseController, um auf die Identität des Nutzers zuzugreifen.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController extends BaseController {

    private final TaskService taskService;

    /**
     * Erzeugt eine neue Instanz des TaskControllers.
     * @param taskService Der Service für die Aufgabenlogik.
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Ruft alle Aufgaben des authentifizierten Nutzers ab.
     * @return Liste der Aufgaben als DTOs.
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks() {
        UUID userId = getAuthenticatedUserId();
        // Hinweis: TaskService benötigt laut deiner Beschreibung eine Methode zum Abrufen.
        // Falls diese noch fehlt, müsste sie im Service ergänzt werden.
        List<TaskDTO> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Erstellt eine neue Aufgabe für den Nutzer.
     * @param dto Die Daten der neuen Aufgabe.
     * @return Das erstellte TaskDTO.
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO dto) {
        UUID userId = getAuthenticatedUserId();
        TaskDTO createdTask = taskService.createTask(userId, dto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Aktualisiert eine bestehende Aufgabe.
     * @param dto Die aktualisierten Daten.
     * @return Das geänderte TaskDTO.
     */
    @PutMapping
    public ResponseEntity<TaskDTO> updateTask(@RequestBody TaskDTO dto) {
        UUID userId = getAuthenticatedUserId();
        TaskDTO updatedTask = taskService.updateTask(userId, dto);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Löscht eine Aufgabe.
     * @param dto Das DTO, welches die zu löschende Aufgabe identifiziert.
     * @return No Content Status.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteTask(@RequestBody TaskDTO dto) {
        UUID userId = getAuthenticatedUserId();
        // Hier ziehen wir die Task-UUID aus dem DTO, um sie an den Service zu geben
        taskService.deleteTask(userId, null);//todo: null durch taskId aus dto ersetzen
        return ResponseEntity.noContent().build();
    }
}
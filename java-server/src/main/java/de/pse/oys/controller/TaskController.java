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
 * @author utgid
 * @version 1.0
 */
@RestController
@RequestMapping("/tasks")
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
        try {
            UUID userId = getAuthenticatedUserId();
            List<TaskDTO> tasks = taskService.getTasksByUserId(userId);
            return ResponseEntity.ok(tasks);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Erstellt eine neue Aufgabe für den Nutzer.
     * @param dto Die Daten der neuen Aufgabe.
     * @return Das erstellte TaskDTO.
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();
            TaskDTO createdTask = taskService.createTask(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Aktualisiert eine bestehende Aufgabe.
     * @param dto Die aktualisierten Daten.
     * @return Das geänderte TaskDTO.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable UUID taskId, @RequestBody TaskDTO dto) {
        try {
            UUID userId = getAuthenticatedUserId();

            TaskDTO updatedTask = taskService.updateTask(userId, taskId, dto);

            return ResponseEntity.ok(updatedTask);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Löscht eine Aufgabe.
     *
     * @param taskId Die ID der zu löschenden Aufgabe.
     * @return No Content Status.
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        try {
            UUID userId = getAuthenticatedUserId();
            taskService.deleteTask(userId, taskId);
            return ResponseEntity.noContent().build();
        }catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
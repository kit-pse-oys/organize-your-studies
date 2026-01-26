package de.pse.oys.service;

import de.pse.oys.domain.Task;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service-Klasse zur Verwaltung von Aufgaben.
 * Verarbeitet die Geschäftslogik für das Erstellen, Aktualisieren und Löschen von Tasks.
 * @author utgid
 * @version 1.0
 */
@Service
public class TaskService {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final TaskRepository taskRepository;

    /**
     * Erzeugt eine neue Instanz des TaskService.
     * @param userRepository Das Repository für Benutzerdaten.
     * @param moduleRepository Das Repository für Moduldaten.
     * @param taskRepository Das Repository für Aufgabendaten.
     */
    public TaskService(UserRepository userRepository, ModuleRepository moduleRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Erstellt eine neue Aufgabe für einen Benutzer.
     * @param userId Die UUID des authentifizierten Benutzers.
     * @param dto Die Daten der zu erstellenden Aufgabe.
     * @return Das erstellte TaskDTO inklusive generierter ID.
     */
    @Transactional
    public TaskDTO createTask(UUID userId, TaskDTO dto) {
        validateData(dto);

        //todo: implementieren
        return dto;
    }

    /**
     * Aktualisiert eine bestehende Aufgabe.
     * @param userId Die UUID des Benutzers (zur Autorisierungsprüfung).
     * @param dto Die neuen Daten der Aufgabe.
     * @return Das aktualisierte TaskDTO.
     */
    @Transactional
    public TaskDTO updateTask(UUID userId, TaskDTO dto) {
        validateData(dto);
    //todo: implementieren
        return dto;
    }

    /**
     * Löscht eine Aufgabe aus dem System.
     * @param userId Die UUID des Benutzers.
     * @param taskId Die UUID der zu löschenden Aufgabe.
     */
    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        //todo: implementieren
    }

    /**
     * Ruft alle Aufgaben ab, die einem bestimmten Benutzer zugeordnet sind.
     *
     * @param userId Die UUID des authentifizierten Benutzers.
     * @return Eine Liste von TaskDTOs, die dem Benutzer gehören.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(UUID userId) {
        // Wir suchen alle Task-Entitäten für die gegebene User-ID im Repository
        return null; //todo: implementieren
    }

    /**
     * Validiert die Eingabedaten eines TaskDTOs.
     * @param dto Das zu prüfende DTO.
     */
    private void validateData(TaskDTO dto) {
        //todo: implementieren
    }

    /**
     * Überführt ein TaskDTO in eine Task-Entität.
     * @param dto Das Quelldaten-Objekt.
     * @return Die vorbereitete Entität.
     */
    private Task mapToEntity(TaskDTO dto) {
        //todo: implementieren
        return null;
    }
}
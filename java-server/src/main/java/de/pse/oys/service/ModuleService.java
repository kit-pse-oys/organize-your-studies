package de.pse.oys.service;

import de.pse.oys.domain.Module;
import de.pse.oys.domain.User;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.dto.ModuleDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service-Klasse für die Verwaltung von Modulen.
 * Kapselt die Geschäftslogik für das Erstellen, Aktualisieren und Löschen von Modulen
 * sowie die Validierung und das Mapping zwischen DTOs und Entitäten[cite: 5].
 * * @author utgid
 * @version 1.1
 */
@Service
@Transactional
public class ModuleService {

    private static final String MSG_USER_NOT_FOUND = "User mit ID %s wurde nicht gefunden.";
    private static final String MSG_MODULE_NOT_OWNED = "Das Modul mit ID %s gehört nicht zum Nutzer oder existiert nicht.";
    private static final String MSG_UPDATE_REQUIRES_ID = "Für ein Update muss die Modul-ID im DTO gesetzt sein.";
    private static final String MSG_TITLE_EMPTY = "Der Modultitel darf nicht leer sein.";

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    /**
     * Erzeugt eine neue Instanz des ModuleService[cite: 5].
     *
     * @param userRepository    Das Repository für Nutzerdaten[cite: 5].
     * @param moduleRepository Das Repository für Moduldaten[cite: 5].
     */
    public ModuleService(UserRepository userRepository, ModuleRepository moduleRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
    }

    /**
     * Erstellt ein neues Modul für einen Nutzer und verknüpft es mit dessen Account[cite: 5].
     *
     * @param userId Die ID des Nutzers, dem das Modul zugeordnet wird[cite: 5].
     * @param dto    Die Daten des zu erstellenden Moduls[cite: 5].
     * @return Das erstellte Modul als {@link ModuleDTO} mit generierter ID[cite: 5].
     * @throws IllegalArgumentException wenn der Nutzer nicht existiert oder Validierungsfehler auftreten.
     */
    public ModuleDTO createModule(UUID userId, ModuleDTO dto) {
        validateData(dto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_USER_NOT_FOUND, userId)));

        Module entity = mapToEntity(dto);

        user.addModule(entity);
        Module saved = moduleRepository.save(entity);
        userRepository.save(user);

        return mapToDto(saved);
    }

    /**
     * Aktualisiert ein bestehendes Modul basierend auf der im DTO enthaltenen ID[cite: 5].
     *
     * @param userId Die ID des Nutzers (zur Autorisierung)[cite: 5].
     * @param dto    Die aktualisierten Moduldaten[cite: 5].
     * @return Das aktualisierte Modul als {@link ModuleDTO}[cite: 5].
     * @throws IllegalArgumentException wenn die ID fehlt oder das Modul nicht existiert.
     */
    public ModuleDTO updateModule(UUID userId, ModuleDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException(MSG_UPDATE_REQUIRES_ID);
        }

        validateData(dto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_USER_NOT_FOUND, userId)));

        Module existing = user.getModules().stream()
                .filter(m -> m.getModuleId().equals(dto.getId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException(String.format(MSG_MODULE_NOT_OWNED, dto.getId())));

        // Mapping der neuen Werte auf die bestehende Entität
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setPriority(dto.getPriority());
        existing.setColorHexCode(dto.getColor());

        Module updated = moduleRepository.save(existing);
        return mapToDto(updated);
    }

    /**
     * Löscht ein Modul permanent aus dem System[cite: 5].
     *
     * @param userId   Die ID des Nutzers (zur Autorisierung)[cite: 5].
     * @param moduleId Die ID des zu löschenden Moduls[cite: 5].
     * @throws IllegalArgumentException wenn das Modul nicht gefunden werden kann.
     */
    public void deleteModule(UUID userId, UUID moduleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(MSG_USER_NOT_FOUND, userId)));

        Module existing = user.getModules().stream()
                .filter(m -> m.getModuleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new SecurityException(String.format(MSG_MODULE_NOT_OWNED, moduleId)));

        // Das Modul wird aus der Liste des Users entfernt und gelöscht
        user.deleteModule(existing);
        moduleRepository.delete(existing);
    }

    /**
     * Validiert die übergebenen Moduldaten auf fachliche Korrektheit[cite: 5].
     *
     * @param dto Das zu validierende DTO[cite: 5].
     * @throws IllegalArgumentException bei fehlenden Pflichtfeldern.
     */
    private void validateData(ModuleDTO dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException(MSG_TITLE_EMPTY);
        }
    }

    /**
     * Wandelt ein {@link ModuleDTO} in die Domänen-Entität {@link Module} um[cite: 5].
     *
     * @param dto Das Quell-DTO[cite: 5].
     * @return Die resultierende Module-Entität[cite: 5].
     */
    private Module mapToEntity(ModuleDTO dto) {
        Module entity = new Module(dto.getTitle(), dto.getPriority());
        entity.setDescription(dto.getDescription());
        entity.setColorHexCode(dto.getColor());
        return entity;
    }

    /**
     * Wandelt eine {@link Module}-Entität in ein {@link ModuleDTO} um.
     * * @param entity Die zu konvertierende Entität.
     * @return Das resultierende Datentransferobjekt.
     */
    private ModuleDTO mapToDto(Module entity) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(entity.getModuleId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPriority(entity.getPriority());
        dto.setColor(entity.getColorHexCode());
        return dto;
    }
}
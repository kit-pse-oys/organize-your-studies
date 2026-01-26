package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service zur Verwaltung der Freizeiträume der Benutzer.
 * Enthält die Geschäftslogik zur Validierung und Speicherung von Zeitfenstern.
 * @author utgid
 * @version 1.0
 */
@Service
public class FreeTimeService {

    private final UserRepository userRepository;
    private final FreeTimeRepository freeTimeRepository;

    public FreeTimeService(UserRepository userRepository, FreeTimeRepository freeTimeRepository) {
        this.userRepository = userRepository;
        this.freeTimeRepository = freeTimeRepository;
    }

    /**
     * Ruft alle Freizeiträume eines bestimmten Nutzers ab.
     * @param userId Die UUID des authentifizierten Benutzers.
     * @return Liste der Freizeiträume als DTOs.
     */
    @Transactional(readOnly = true)
    public List<FreeTimeDTO> getFreeTimesByUserId(UUID userId) {
        return freeTimeRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Erstellt einen neuen Freizeitraum für den Nutzer.
     * @param userId Die UUID des Benutzers.
     * @param dto Die Daten des neuen Zeitfensters.
     * @return Das gespeicherte DTO mit generierter ID.
     */
    @Transactional
    public FreeTimeDTO createFreeTime(UUID userId, FreeTimeDTO dto) {
        //todo: implementieren
        return dto;
    }

    /**
     * Aktualisiert einen bestehenden Freizeitraum.
     * @param userId Die UUID des Nutzers zur Autorisierungsprüfung.
     * @param dto Die aktualisierten Daten.
     * @return Das geänderte DTO.
     */
    @Transactional
    public FreeTimeDTO updateFreeTime(UUID userId, FreeTimeDTO dto) {
        //todo: implementieren
        return dto;
    }

    /**
     * Löscht einen Freizeitraum.
     * @param userId Die UUID des Nutzers.
     * @param freeTimeId Die ID des zu löschenden Objekts.
     */
    @Transactional
    public void deleteFreeTime(UUID userId, UUID freeTimeId) {
        //todo: implementieren
    }

    /**
     * Validiert die logische Korrektheit der Zeitangaben.
     */
    private void validateData(FreeTimeDTO dto) {
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new IllegalArgumentException("Start- und Endzeit müssen gesetzt sein.");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new IllegalArgumentException("Die Startzeit muss vor der Endzeit liegen.");
        }
    }


    private FreeTimeDTO mapToDto(FreeTime entity) {
        //todo: implementieren
        return null;
    }
}
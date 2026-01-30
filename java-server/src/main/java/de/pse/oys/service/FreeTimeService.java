package de.pse.oys.service;

import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.domain.FreeTime;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FreeTimeService {

    private static final String ERR_TIME_CONSISTENCY = "Startzeit muss vor der Endzeit liegen.";
    private static final String ERR_USER_NOT_FOUND = "Nutzer existiert nicht.";
    private static final String ERR_OVERLAP = "Der Zeitraum überschneidet sich mit einer bestehenden Freizeit.";

    private final UserRepository userRepository;
    private final FreeTimeRepository freeTimeRepository;

    public FreeTimeService(UserRepository userRepository, FreeTimeRepository freeTimeRepository) {
        this.userRepository = userRepository;
        this.freeTimeRepository = freeTimeRepository;
    }

    public FreeTimeDTO createFreeTime(UUID userId, FreeTimeDTO dto) {
        validateData(dto);

        // TODO: user existence prüfen (userRepository)
        // TODO: dto -> entity mappen
        FreeTime entity = mapToEntity(dto);

        // TODO: entity dem User zuordnen (falls Modell das vorsieht)
        FreeTime saved = freeTimeRepository.save(entity);

        // TODO: entity -> dto mappen
        return mapToDto(saved);
    }

    public FreeTimeDTO updateFreeTime(UUID userId, FreeTimeDTO dto) {
        validateData(dto);

        // TODO: user existence prüfen (userRepository)
        // TODO: bestehende FreeTime laden (z.B. über dto.getId() falls vorhanden)
        // TODO: Felder updaten + speichern
        // FreeTime existing = ...
        // FreeTime saved = freeTimeRepository.save(existing);

        // Platzhalter:
        FreeTime saved = freeTimeRepository.save(mapToEntity(dto));
        return mapToDto(saved);
    }

    public void deleteFreeTime(UUID userId, UUID freeTimeId) {
        // TODO: user existence prüfen (userRepository)
        // TODO: ownership prüfen (gehört die FreeTime zum User?)
        freeTimeRepository.deleteById(freeTimeId);
    }

    @Override
    public void validateData(FreeTimeDTO dto) {
// 1. Zeit-Logik über die Entity-Methode prüfen
        FreeTime tempEntity = mapToEntity(dto);
        if (!tempEntity.isValidTimeRange()) {
            throw new ValidationException(ERR_TIME_CONSISTENCY);
        }
        // 2. Nutzer-Existenz prüfen
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ValidationException(ERR_USER_NOT_FOUND);
        }
        // 3. Überschneidungen prüfen
        boolean hasOverlap = freeTimeRepository.findByUserId(dto.getUserId()).stream()
                .filter(existing -> !existing.getId().equals(dto.getId()))
                .anyMatch(existing -> dto.getStartTime().isBefore(existing.getEndTime())
                        && dto.getEndTime().isAfter(existing.getStartTime()));
        if (hasOverlap) {
            throw new ValidationException(ERR_OVERLAP);
        }
    }

    private FreeTime mapToEntity(FreeTimeDTO dto) {
        FreeTime entity = new FreeTime();
        // TODO: je nach Modell:
        // - dto.weekly == true -> RecurringFreeTime
        // - sonst -> SingleFreeTime
        //
        // return new RecurringFreeTime(...);
        // return new SingleFreeTime(...);

        throw new UnsupportedOperationException("TODO: mapToEntity noch implementieren");
    }

    private FreeTimeDTO mapToDto(FreeTime entity) {
        // TODO: Entity-Felder in DTO übertragen
        // return new FreeTimeDTO(...);

        return new FreeTimeDTO(); // Platzhalter
    }

    // Hilfsmethoden
    private boolean isOverlapping(FreeTimeDTO dto, FreeTime existing) {
        return dto.getStartTime().isBefore(existing.getEndTime())
                && dto.getEndTime().isAfter(existing.getStartTime());
    }

    private void checkNullValues(FreeTimeDTO dto) {
        if (dto == null) {
            throw new ValidationException("Das FreeTime-Objekt darf nicht null sein.");
        }
        if (dto.getStartTime() == null) {
            throw new ValidationException("Die Startzeit darf nicht leer sein.");
        }
        if (dto.getEndTime() == null) {
            throw new ValidationException("Die Endzeit darf nicht leer sein.");
        }
        if (dto.getUserId() == null) {
            throw new ValidationException("Die Benutzer-ID muss angegeben werden.");
        }
    }
}

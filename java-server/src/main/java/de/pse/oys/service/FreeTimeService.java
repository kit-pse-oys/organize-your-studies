package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Kapselt die Geschäftslogik für das Erstellen und Verwalten von Freizeiten.
 */
@Service
@Transactional
public class FreeTimeService {

    private static final String MSG_REQUIRED_FIELDS_MISSING = "Pflichtfelder fehlen.";
    private static final String MSG_INVALID_RANGE = "Die Startzeit muss vor der Endzeit liegen.";
    private static final String MSG_USER_NOT_FOUND = "User existiert nicht.";
    private static final String MSG_FREETIME_NOT_FOUND = "FreeTime existiert nicht.";
    private static final String MSG_FORBIDDEN = "Zugriff verweigert.";
    private static final String MSG_OVERLAP = "Die Freizeit überschneidet sich mit einem bestehenden Eintrag.";
    private static final String MSG_TYPE_CHANGE_NOT_SUPPORTED = "Recurrence-Typ kann per Update nicht geändert werden.";

    private final UserRepository userRepository;
    private final FreeTimeRepository freeTimeRepository;

    public FreeTimeService(UserRepository userRepository, FreeTimeRepository freeTimeRepository) {
        this.userRepository = userRepository;
        this.freeTimeRepository = freeTimeRepository;
    }

    /**
     * Koordiniert die Erstellung: validiert, prüft Überschneidungen, mappt DTO -> Entity und speichert.
     *
     * @param userId Nutzer-ID
     * @param dto    Eingabedaten
     * @return gespeicherte Freizeit als DTO
     */
    public FreeTimeDTO createFreeTime(UUID userId, FreeTimeDTO dto) {
        requireUserExists(userId);
        validate(dto);
        ensureNoOverlap(userId, dto, null);
        FreeTime saved = freeTimeRepository.save(toEntity(userId, dto));
        return toDto(saved);
    }

    /**
     * Koordiniert das Update: validiert, lädt bestehende Freizeit, prüft Ownership, verhindert Typwechsel
     * und speichert die Änderungen.
     *
     * @param userId     Nutzer-ID
     * @param freeTimeId ID der Freizeit
     * @param dto        neue Daten
     * @return aktualisierte Freizeit als DTO
     */
    public FreeTimeDTO updateFreeTime(UUID userId, UUID freeTimeId, FreeTimeDTO dto) {
        requireUserExists(userId);
        requireId(freeTimeId);
        validate(dto);

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FREETIME_NOT_FOUND));

        assertOwner(existing, userId);

        if (dto.isWeekly() != isWeekly(existing)) {
            throw new ValidationException(MSG_TYPE_CHANGE_NOT_SUPPORTED);
        }

        ensureNoOverlap(userId, dto, existing.getFreeTimeId());

        applyUpdate(existing, dto);

        return toDto(freeTimeRepository.save(existing));
    }

    /**
     * Koordiniert das Löschen: validiert, lädt bestehende Freizeit, prüft Ownership und löscht.
     *
     * @param userId     Nutzer-ID
     * @param freeTimeId ID der Freizeit
     */
    public void deleteFreeTime(UUID userId, UUID freeTimeId) {
        requireUserExists(userId);
        requireId(freeTimeId);

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FREETIME_NOT_FOUND));

        assertOwner(existing, userId);

        freeTimeRepository.delete(existing);
    }

    /**
     * Prüft die logische Konsistenz der Eingabedaten und wirft bei Fehlern eine {@link ValidationException}.
     *
     * @param dto Eingabedaten
     */
    protected void validate(FreeTimeDTO dto) {
        if (dto == null
                || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getDate() == null
                || dto.getStartTime() == null
                || dto.getEndTime() == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new ValidationException(MSG_INVALID_RANGE);
        }
    }

    /**
     * Transformiert das DTO in das Domänenmodell.
     *
     * @param userId Nutzer-ID
     * @param dto    Eingabedaten
     * @return Domain-Entität (Recurring oder Single)
     */
    protected FreeTime toEntity(UUID userId, FreeTimeDTO dto) {
        if (dto.isWeekly()) {
            return new RecurringFreeTime(
                    userId,
                    dto.getTitle(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getDate().getDayOfWeek()
            );
        }

        return new SingleFreeTime(
                userId,
                dto.getTitle(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getDate()
        );
    }

    // -------------------------
    // Helpers
    // -------------------------

    /** DB-seitiger Overlap-Check. */
    private void ensureNoOverlap(UUID userId, FreeTimeDTO dto, UUID ignoreId) {
        String weekday = dto.getDate().getDayOfWeek().name();

        boolean overlap = freeTimeRepository.existsOverlap(
                userId,
                dto.getDate(),
                weekday,
                dto.getStartTime(),
                dto.getEndTime(),
                ignoreId
        );

        if (overlap) {
            throw new ValidationException(MSG_OVERLAP);
        }
    }

    /** Validiert, dass die ID gesetzt ist. */
    private void requireId(UUID id) {
        if (id == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }
    }

    /** Validiert, dass der Nutzer existiert. */
    private void requireUserExists(UUID userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(MSG_USER_NOT_FOUND);
        }
    }

    /** Prüft, ob die Freizeit dem Nutzer gehört. */
    private void assertOwner(FreeTime ft, UUID userId) {
        if (!Objects.equals(ft.getUserId(), userId)) {
            throw new AccessDeniedException(MSG_FORBIDDEN);
        }
    }

    /** Prüft, ob die Freizeit wöchentlich wiederkehrend ist. */
    private boolean isWeekly(FreeTime ft) {
        return ft.getRecurrenceType() == RecurrenceType.WEEKLY;
    }

    /** Überträgt die DTO-Werte auf die bestehende Freizeit-Entität. */
    private void applyUpdate(FreeTime existing, FreeTimeDTO dto) {
        existing.setTitle(dto.getTitle());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.applyDtoDate(dto.getDate());
    }

    /** Mappt eine Freizeit-Entität auf ein DTO. */
    private FreeTimeDTO toDto(FreeTime entity) {
        FreeTimeDTO dto = new FreeTimeDTO();
        dto.setTitle(entity.getTitle());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setWeekly(entity.getRecurrenceType() == RecurrenceType.WEEKLY);
        dto.setDate(entity.getRepresentativeDate());
        return dto;
    }
}


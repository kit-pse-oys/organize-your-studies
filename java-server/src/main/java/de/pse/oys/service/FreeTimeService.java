package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service für das Verwalten von Freizeitblöcken eines Nutzers.
 *
 * @author uqvfm
 * @version 1.0
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

    /**
     * Erstellt einen neuen Service.
     *
     * @param userRepository Repository für Nutzer
     * @param freeTimeRepository Repository für Freizeitblöcke
     */
    public FreeTimeService(UserRepository userRepository, FreeTimeRepository freeTimeRepository) {
        this.userRepository = userRepository;
        this.freeTimeRepository = freeTimeRepository;
    }

    /**
     * Koordiniert die Erstellung: validiert, prüft Überschneidungen, mappt DTO -> Entity und speichert.
     *
     * @param userId Nutzer-ID
     * @param dto Eingabedaten (temporär: {@link FreeTimeDTO})
     * @return angelegte Freizeit als DTO
     */
    @Transactional
    public UUID createFreeTime(UUID userId, FreeTimeDTO dto) throws ResourceNotFoundException, ValidationException {
        requireUserExists(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_USER_NOT_FOUND));

        validate(dto);
        ensureNoOverlap(userId, dto, null);

        FreeTime newFreeTime = toEntity(userId, dto);

        user.addFreeTime(newFreeTime);
        userRepository.saveAndFlush(user); // speichert auch die neue Freizeit durch Cascade

        return newFreeTime.getFreeTimeId();
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
    public FreeTimeDTO updateFreeTime(UUID userId, UUID freeTimeId, FreeTimeDTO dto) throws ResourceNotFoundException, ValidationException, AccessDeniedException {
        requireUserExists(userId);
        requireId(freeTimeId);
        validate(dto);

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FREETIME_NOT_FOUND));

        assertOwner(existing, userId);

        if (dto.isWeekly() != existing.isWeekly()) {
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
    public void deleteFreeTime(UUID userId, UUID freeTimeId) throws ResourceNotFoundException, ValidationException, AccessDeniedException {
        requireUserExists(userId);
        requireId(freeTimeId);

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FREETIME_NOT_FOUND));

        assertOwner(existing, userId);

        freeTimeRepository.delete(existing);
    }

    /**
     * Liefert alle Freizeiten eines Nutzers als Liste von Wrapper-Objekten.
     * Jeder Eintrag enthält die ID der Freizeit sowie das zugehörige {@link FreeTimeDTO}
     * im Format {@code { "id": "...", "data": { ... } }}.
     *
     * @param userId ID des Nutzers, dessen Freizeiten abgefragt werden.
     * @return Liste aller Freizeiten des Nutzers (leer, wenn keine vorhanden sind).
     * @throws NullPointerException     wenn {@code userId} {@code null} ist.
     * @throws ResourceNotFoundException wenn der Nutzer nicht existiert.
     */
    public List<WrapperDTO<FreeTimeDTO>> getFreeTimesByUserId(UUID userId) throws ResourceNotFoundException {
        Objects.requireNonNull(userId, "userId");
        requireUserExists(userId);

        return freeTimeRepository.findAllByUserId(userId).stream()
                .map(freeTime -> new WrapperDTO<>(getId(freeTime), toDto(freeTime)))
                .toList();
    }

    /**
     * Prüft die logische Konsistenz der Eingabedaten und wirft bei Fehlern eine {@link ValidationException}.
     *
     * @param dto Eingabedaten
     */
    protected void validate(FreeTimeDTO dto) throws ValidationException {
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

    /** Overlap-Check. */
    private void ensureNoOverlap(UUID userId, FreeTimeDTO dto, UUID ignoreId) {

        boolean overlap = hasOverlap(
                userId,
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                ignoreId
        );

        if (overlap) {
            throw new ValidationException(MSG_OVERLAP);
        }
    }

    /** Overlap-Check Subroutine. */
    private boolean hasOverlap(UUID userId,
                               java.time.LocalDate date,
                               java.time.LocalTime startTime,
                               java.time.LocalTime endTime,
                               UUID ignoreId) {

        List<FreeTime> candidates = freeTimeRepository.findAllByUserId(userId);

        for (FreeTime ft : candidates) {
            if (ft == null) continue;

            UUID ftId = ft.getFreeTimeId();
            if (ignoreId != null && ignoreId.equals(ftId)) {
                continue;
            }

            // gilt am Datum? (Single: Datum, Weekly: Wochentag)
            if (!ft.occursOn(date)) {
                continue;
            }

            // Overlap: [start, end)
            if (ft.getStartTime().isBefore(endTime) && startTime.isBefore(ft.getEndTime())) {
                return true;
            }
        }

        return false;
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

    /**
     * Extrahiert die ID aus einer {@link FreeTime}-Entität.
     */
    private UUID getId(FreeTime freeTime) {
        return freeTime.getFreeTimeId();
    }
}

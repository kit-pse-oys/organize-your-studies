package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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

    private static final int DAYS_PER_WEEK = 7;

    private static final String MSG_UPDATE_REQUIRES_ID = "Für updateFreeTime muss dto.id gesetzt sein.";
    private static final String MSG_FREE_TIME_NOT_FOUND_TEMPLATE = "FreeTime mit ID %s wurde nicht gefunden.";
    private static final String MSG_FREE_TIME_NOT_OWNED = "FreeTime gehört nicht zum angegebenen User.";
    private static final String MSG_USER_NOT_FOUND_TEMPLATE = "User mit ID %s wurde nicht gefunden.";

    private static final String MSG_REQUIRED_FIELDS_MISSING =
            "Pflichtfelder fehlen (title/date/startTime/endTime).";
    private static final String MSG_INVALID_RANGE =
            "Ungültiger Zeitraum: startTime muss vor endTime liegen.";
    private static final String MSG_OVERLAP =
            "Freizeit überschneidet sich mit einer bestehenden Freizeit.";

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
     * Legt einen neuen Freizeitblock für einen Nutzer an.
     *
     * @param userId Nutzer-ID
     * @param dto Eingabedaten (temporär: {@link FreeTimeDTO})
     * @return angelegte Freizeit als DTO
     */
    public FreeTimeDTO createFreeTime(UUID userId, FreeTimeDTO dto) {
        User user = loadUser(userId);
        validateData(user, dto, null);

        // id bleibt entweder aus DTO oder wird neu generiert (falls DTO null liefert)
        UUID id = (dto.getId() != null) ? dto.getId() : UUID.randomUUID();
        FreeTime entity = mapToEntity(dto);

        // falls eure Entities die ID nicht im Konstruktor setzen, ist id hier nur "genutzt"
        // (im Zweifel: entity.setFreeTimeId(id); falls es sowas bei euch gibt)
        // -> wir lassen das Verhalten wie im Original unverändert.

        user.addFreeTime(entity);
        userRepository.save(user); // Cascade.ALL auf freeTimes

        return mapToDto(entity);
    }

    /**
     * Aktualisiert einen bestehenden Freizeitblock.
     *
     * @param userId Nutzer-ID
     * @param dto neue Daten (dto.id muss gesetzt sein)
     * @return aktualisierte Freizeit als DTO
     */
    public FreeTimeDTO updateFreeTime(UUID userId, FreeTimeDTO dto) {
        User user = loadUser(userId);

        UUID freeTimeId = dto.getId();
        if (freeTimeId == null) {
            throw new IllegalArgumentException(MSG_UPDATE_REQUIRES_ID);
        }

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(MSG_FREE_TIME_NOT_FOUND_TEMPLATE, freeTimeId)));

        if (!belongsToUser(user, freeTimeId)) {
            throw new IllegalArgumentException(MSG_FREE_TIME_NOT_OWNED);
        }

        validateData(user, dto, freeTimeId);

        boolean wantsWeekly = dto.isWeekly();
        boolean isWeekly = existing.getRecurrenceType() == RecurrenceType.WEEKLY;

        // Wechsel zwischen Unterklassen (ONCE <-> WEEKLY) erfordert eine neue Entity.
        if (wantsWeekly != isWeekly) {
            user.deleteFreeTime(existing);
            freeTimeRepository.delete(existing);

            FreeTime replacement = mapToEntity(dto);
            user.addFreeTime(replacement);

            userRepository.save(user);
            return mapToDto(replacement);
        }

        // Typ bleibt gleich: Felder direkt aktualisieren.
        existing.setTitle(dto.getTitle());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());

        if (existing.getRecurrenceType() == RecurrenceType.WEEKLY) {
            ((RecurringFreeTime) existing).setDayOfWeek(dto.getDate().getDayOfWeek());
        } else {
            ((SingleFreeTime) existing).setDate(dto.getDate());
        }

        freeTimeRepository.save(existing);
        userRepository.save(user);

        return mapToDto(existing);
    }

    /**
     * Löscht einen Freizeitblock.
     *
     * @param userId Nutzer-ID
     * @param freeTimeId Freizeit-ID
     */
    public void deleteFreeTime(UUID userId, UUID freeTimeId) {
        User user = loadUser(userId);

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(MSG_FREE_TIME_NOT_FOUND_TEMPLATE, freeTimeId)));

        if (!belongsToUser(user, freeTimeId)) {
            throw new IllegalArgumentException(MSG_FREE_TIME_NOT_OWNED);
        }

        user.deleteFreeTime(existing);
        freeTimeRepository.delete(existing);
        userRepository.save(user);
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(MSG_USER_NOT_FOUND_TEMPLATE, userId)));
    }

    private void validateData(User user, FreeTimeDTO dto, UUID ignoreId) {
        if (dto == null
                || isBlank(dto.getTitle())
                || dto.getDate() == null
                || dto.getStartTime() == null
                || dto.getEndTime() == null) {
            throw new IllegalArgumentException(MSG_REQUIRED_FIELDS_MISSING);
        }

        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new IllegalArgumentException(MSG_INVALID_RANGE);
        }

        List<FreeTime> existing = user.getFreeTimes();
        if (existing == null) {
            return;
        }

        for (FreeTime ft : existing) {
            if (ft == null) {
                continue;
            }
            if (ignoreId != null && ignoreId.equals(ft.getFreeTimeId())) {
                continue;
            }

            if (!occursSameDay(ft, dto.getDate())) {
                continue;
            }

            if (overlaps(ft.getStartTime(), ft.getEndTime(), dto.getStartTime(), dto.getEndTime())) {
                throw new IllegalArgumentException(MSG_OVERLAP);
            }
        }
    }

    private boolean occursSameDay(FreeTime existing, LocalDate dtoDate) {
        if (dtoDate == null) {
            return false;
        }

        if (existing.getRecurrenceType() == RecurrenceType.WEEKLY) {
            DayOfWeek dow = ((RecurringFreeTime) existing).getDayOfWeek();
            return dow != null && dow == dtoDate.getDayOfWeek();
        }

        if (existing.getRecurrenceType() == RecurrenceType.ONCE) {
            LocalDate d = ((SingleFreeTime) existing).getDate();
            return d != null && d.equals(dtoDate);
        }

        return false;
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        // Intervall-Logik: [start, end)
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private FreeTime mapToEntity(FreeTimeDTO dto) {
        if (dto.isWeekly()) {
            return new RecurringFreeTime(
                    dto.getTitle(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getDate().getDayOfWeek()
            );
        }
        return new SingleFreeTime(dto.getTitle(), dto.getStartTime(), dto.getEndTime(), dto.getDate());
    }

    private FreeTimeDTO mapToDto(FreeTime ft) {
        boolean weekly = ft.getRecurrenceType() == RecurrenceType.WEEKLY;

        LocalDate date;
        if (weekly) {
            DayOfWeek dow = ((RecurringFreeTime) ft).getDayOfWeek();
            LocalDate today = LocalDate.now();
            int delta = (dow.getValue() - today.getDayOfWeek().getValue() + DAYS_PER_WEEK) % DAYS_PER_WEEK;
            date = today.plusDays(delta);
        } else {
            date = ((SingleFreeTime) ft).getDate();
        }

        return new FreeTimeDTO(ft.getFreeTimeId(), ft.getTitle(), date, ft.getStartTime(), ft.getEndTime(), weekly);
    }

    private boolean belongsToUser(User user, UUID freeTimeId) {
        List<FreeTime> list = user.getFreeTimes();
        if (list == null) {
            return false;
        }

        for (FreeTime ft : list) {
            if (ft != null && Objects.equals(ft.getFreeTimeId(), freeTimeId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

package de.pse.oys.service;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.exceptions.ValidationException;
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
     * @param userRepository     Repository für Nutzer
     * @param freeTimeRepository Repository für Freizeitblöcke
     */
    public FreeTimeService(UserRepository userRepository, FreeTimeRepository freeTimeRepository) {
        this.userRepository = userRepository;
        this.freeTimeRepository = freeTimeRepository;
    }

    /**
     * Legt einen neuen Freizeitblock für einen Nutzer an.
     *
     * <p><b>Hinweis zur Persistierung:</b> Im Entwurfsheft ist die Speicherung über das
     * {@link FreeTimeRepository} beschrieben. In unserer Implementierung ist die Beziehung
     * {@code User -> freeTimes} jedoch unidirektional mit {@code @OneToMany + @JoinColumn(name="user_id")}
     * modelliert, d. h. der {@link User} ist die owning side. Damit der Fremdschlüssel {@code user_id}
     * zuverlässig gesetzt wird, wird die neue Freizeit dem Nutzer hinzugefügt und der Nutzer gespeichert
     * (Cascade-Persistierung).</p>
     *
     * @param userId Nutzer-ID
     * @param dto    Eingabedaten
     * @return angelegte Freizeit als DTO
     * @throws ValidationException      wenn Eingabedaten ungültig sind (siehe {@link #validateData(User, FreeTimeDTO, UUID)})
     * @throws IllegalArgumentException wenn der Nutzer nicht existiert
     */
    public FreeTimeDTO createFreeTime(UUID userId, FreeTimeDTO dto) {
        User user = loadUser(userId);
        validateData(user, dto, null);

        FreeTime entity = mapToEntity(dto);

        user.addFreeTime(entity);
        userRepository.save(user);

        return mapToDto(entity);
    }

    /**
     * Aktualisiert einen bestehenden Freizeitblock.
     *
     * @param userId     Nutzer-ID
     * @param freeTimeId ID der zu aktualisierenden Freizeit
     * @param dto        neue Daten
     * @return aktualisierte Freizeit als DTO
     * @throws ValidationException      wenn Eingabedaten ungültig sind (siehe {@link #validateData(User, FreeTimeDTO, UUID)})
     * @throws IllegalArgumentException wenn Nutzer/FreeTime nicht existieren oder nicht zusammengehören
     */
    public FreeTimeDTO updateFreeTime(UUID userId, UUID freeTimeId, FreeTimeDTO dto) {
        User user = loadUser(userId);

        FreeTime existing = freeTimeRepository.findById(freeTimeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(MSG_FREE_TIME_NOT_FOUND_TEMPLATE, freeTimeId)));

        if (!belongsToUser(user, freeTimeId)) {
            throw new IllegalArgumentException(MSG_FREE_TIME_NOT_OWNED);
        }

        validateData(user, dto, freeTimeId);

        boolean wantsWeekly = dto.isWeekly();
        boolean isWeekly = existing.getRecurrenceType() == RecurrenceType.WEEKLY;

        // Typwechsel: alte löschen, neue aus DTO erzeugen und über Repository speichern
        if (wantsWeekly != isWeekly) {
            user.deleteFreeTime(existing);
            freeTimeRepository.delete(existing);

            FreeTime replacement = mapToEntity(dto);
            user.addFreeTime(replacement);

            FreeTime saved = freeTimeRepository.save(replacement);
            return mapToDto(saved);
        }

        // Typ bleibt gleich: Felder aktualisieren und über Repository speichern
        existing.setTitle(dto.getTitle());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());

        if (isWeekly) {
            ((RecurringFreeTime) existing).setDayOfWeek(dto.getDate().getDayOfWeek());
        } else {
            ((SingleFreeTime) existing).setDate(dto.getDate());
        }

        FreeTime saved = freeTimeRepository.save(existing);
        return mapToDto(saved);
    }

    /**
     * Löscht einen Freizeitblock.
     *
     * @param userId     Nutzer-ID
     * @param freeTimeId Freizeit-ID
     * @throws IllegalArgumentException wenn Nutzer/FreeTime nicht existieren oder nicht zusammengehören
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
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(MSG_USER_NOT_FOUND_TEMPLATE, userId)));
    }

    /**
     * Prüft die logische Konsistenz der Eingabedaten und wirft bei Verletzung der Regeln eine {@link ValidationException}.
     *
     * @param user     der zugehörige Nutzer (für Overlap-Prüfung)
     * @param dto      Eingabedaten
     * @param ignoreId optional: ID, die bei Overlap-Prüfung ignoriert wird (z. B. beim Update)
     * @throws ValidationException bei fehlenden Pflichtfeldern, ungültigem Zeitraum oder Überschneidung
     */
    private void validateData(User user, FreeTimeDTO dto, UUID ignoreId) {
        if (dto == null
                || isBlank(dto.getTitle())
                || dto.getDate() == null
                || dto.getStartTime() == null
                || dto.getEndTime() == null) {
            throw new ValidationException(MSG_REQUIRED_FIELDS_MISSING);
        }

        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new ValidationException(MSG_INVALID_RANGE);
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
                throw new ValidationException(MSG_OVERLAP);
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

    /**
     * Transformiert das DTO in das Domänenmodell.
     *
     * @param dto Eingabedaten
     * @return Domain-Entity (SingleFreeTime oder RecurringFreeTime)
     */
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

        return new FreeTimeDTO(ft.getTitle(), date, ft.getStartTime(), ft.getEndTime(), weekly);
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

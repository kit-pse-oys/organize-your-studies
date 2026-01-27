package de.pse.oys.service;

import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.InvalidDtoException;
import de.pse.oys.dto.QuestionnaireDTO;
import de.pse.oys.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * QuestionnaireService – Service für die Verarbeitung und Speicherung der Lernpräferenzen aus dem Fragebogen.
 * Verarbeitet die übermittelten Daten, validiert diese und speichert sie persistent.
 * <p>
 * Prüft auf die Existenz des Nutzers und ob dieser bereits Präferenzen gesetzt hat.
 * Bei bestehenden Präferenzen werden diese aktualisiert (Entitätbleibt erhalten), ansonsten neu angelegt.
 *
 * @author uhupo
 * @version 1.0
 */
@Service
public class QuestionnaireService {
    private static final String ERR_USER_NOT_FOUND = "Kein Nutzer gefunden mit der ID: %s";
    private static final String ERR_INVALID_PREFERENCES = "Die angegebenen Lernpräferenzen sind ungültig.";
    private static final int DAILY_HOURS_LIMITER = 24;
    private final UserRepository userRepository;

    /**
     * Konstruktor für QuestionnaireService.
     * Nutzt Dependency Injection, um das UserRepository bereitzustellen.
     * @param userRepository das UserRepository für persistierende Nutzeroperationen.
     */
    public QuestionnaireService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verarbeitet und speichert die vom Nutzer übermittelten Lernpräferenzen aus dem Fragebogen.
     * Wenn der Nutzer bereits Präferenzen hat, werden diese aktualisiert (UUID bleibt gleich).
     * Ansonsten wird eine neue Präferenz erstellt und dem Nutzer zugewiesen.
     * @param userId die eindeutige Id des Nutzers.
     * @param questionnaireDTO die übermittelten Lernpräferenzen als DTO.
     * @throws InvalidDtoException wenn das DTO ungültig ist.
     * @throws EntityNotFoundException wenn kein Nutzer mit der angegebenen ID existiert.
     * @throws IllegalArgumentException wenn die Präferenzen semantisch ungültig sind.
     */
    public void submitQuestionnaire(UUID userId, QuestionnaireDTO questionnaireDTO)
            throws InvalidDtoException, EntityNotFoundException, IllegalArgumentException {

        // Nur wenn der Nutzer existiert, werden die Präferenzen gesetzt, sonst verwerfen wir die Anfrage
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERR_USER_NOT_FOUND, userId)));

        // Validierung der Eingaben aus dem Fragebogen auf Semantik und Null-Safety,
        // wenn ein Wert ungültig ist, wird eine IllegalArgumentException geworfen
        validatePreferences(questionnaireDTO);

        int maxUnitDuration = questionnaireDTO.getMaxUnitDuration();
        int minUnitDuration = questionnaireDTO.getMinUnitDuration();
        int maxDayLoad = questionnaireDTO.getMaxDayLoad();
        int breakDuration = questionnaireDTO.getPreferredPauseDuration();
        int deadlineBufferDays = questionnaireDTO.getTimeBeforeDeadlines();

        Set<TimeSlot> preferredStudyTimes = questionnaireDTO.getPreferredStudyTimes() != null
                ? questionnaireDTO.getPreferredStudyTimes()
                : new HashSet<>();

        Set<DayOfWeek> preferredStudyDays = questionnaireDTO.getPreferredStudyDays() != null
                ? questionnaireDTO.getPreferredStudyDays()
                : new HashSet<>();


        LearningPreferences existingPreferences = user.getPreferences();
        if (existingPreferences == null) {
            existingPreferences = new LearningPreferences(
                    minUnitDuration,
                    maxUnitDuration,
                    maxDayLoad,
                    breakDuration,
                    deadlineBufferDays,
                    preferredStudyTimes,
                    preferredStudyDays
            );
        } else {
            existingPreferences.setMinUnitDurationMinutes(minUnitDuration);
            existingPreferences.setMaxUnitDurationMinutes(maxUnitDuration);
            existingPreferences.setMaxDailyWorkloadHours(maxDayLoad);
            existingPreferences.setBreakDurationMinutes(breakDuration);
            existingPreferences.setDeadlineBufferDays(deadlineBufferDays);
            existingPreferences.setPreferredTimeSlots(preferredStudyTimes);
            existingPreferences.setPreferredDays(preferredStudyDays);
        }

        user.setPreferences(existingPreferences);
        userRepository.save(user);
    }


    /**
     * Validiert die im Fragebogen angegebenen Präferenzen.
     *
     * @param questionnaireDTO das zu validierende QuestionnaireDTO.
     * @throws IllegalArgumentException wenn die Präferenzen ungültig sind.
     */
    private void validatePreferences(QuestionnaireDTO questionnaireDTO) throws IllegalArgumentException, InvalidDtoException {
        if (questionnaireDTO == null) {
            throw new IllegalArgumentException(ERR_INVALID_PREFERENCES);
        }

        // Kein primitiver Typ, da null übergeben werden kann und geprüft werden muss
        Integer minUnitDuration = questionnaireDTO.getMinUnitDuration();
        Integer maxUnitDuration = questionnaireDTO.getMaxUnitDuration();
        Integer maxDayLoad = questionnaireDTO.getMaxDayLoad();
        Integer breakDuration = questionnaireDTO.getPreferredPauseDuration();
        Integer deadlineBufferDays = questionnaireDTO.getTimeBeforeDeadlines();

        // Null prüfen
        if (minUnitDuration == null || maxUnitDuration == null || maxDayLoad == null
                || breakDuration == null || deadlineBufferDays == null) {
            throw new IllegalArgumentException(ERR_INVALID_PREFERENCES);
        }

        if (minUnitDuration <= 0 || maxUnitDuration <= 0 || maxDayLoad <= 0 || breakDuration < 0 || deadlineBufferDays < 0) {
            throw new IllegalArgumentException(ERR_INVALID_PREFERENCES);
        }
        if (maxDayLoad > DAILY_HOURS_LIMITER) {
            throw new IllegalArgumentException(ERR_INVALID_PREFERENCES);
        }
        if (minUnitDuration > maxUnitDuration) {
            throw new IllegalArgumentException(ERR_INVALID_PREFERENCES);
        }
    }

    /**
     * Prüft, ob der Nutzer mit der angegebenen ID Lernpräferenzen gesetzt hat.
     *
     * @param userId die eindeutige Id des Nutzers.
     * @return true, wenn der Nutzer bereits Lernpräferenzen hat, sonst false.
     * @throws EntityNotFoundException wenn kein Nutzer mit der angegebenen ID existiert.
     */
    public boolean hasLearningPreferences(UUID userId) throws EntityNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ERR_USER_NOT_FOUND, userId)));
        return user.getPreferences() != null;
    }
}

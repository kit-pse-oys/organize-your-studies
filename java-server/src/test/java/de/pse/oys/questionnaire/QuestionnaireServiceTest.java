package de.pse.oys.questionnaire;

import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.QuestionnaireDTO;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.QuestionnaireService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * QuestionnaireServiceTest – Unit-Tests für den QuestionnaireService.
 *
 * @author uhupo
 * @version 1.0
 */

@SpringBootTest
@ActiveProfiles("test")
class QuestionnaireServiceTest {

    private UserRepository userRepository;
    private QuestionnaireService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new QuestionnaireService(userRepository);
    }

    @Test
    void testSubmitQuestionnaire_createsNewPreferences() {
        UUID userId = UUID.randomUUID();
        LocalUser user = new LocalUser("testuser", "hash");
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setMinUnitDuration(30);
        dto.setMaxUnitDuration(90);
        dto.setMaxDayLoad(6);
        dto.setPreferredPauseDuration(10);
        dto.setTimeBeforeDeadlines(2);
        dto.setPreferredStudyTimes(Set.of(TimeSlot.MORNING, TimeSlot.EVENING));
        dto.setPreferredStudyDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        service.submitQuestionnaire(userId, dto);

        LearningPreferences prefs = user.getPreferences();
        assertNotNull(prefs);
        assertEquals(30, prefs.getMinUnitDurationMinutes());
        assertEquals(90, prefs.getMaxUnitDurationMinutes());
        assertEquals(6, prefs.getMaxDailyWorkloadHours());
        assertEquals(10, prefs.getBreakDurationMinutes());
        assertEquals(2, prefs.getDeadlineBufferDays());
        assertEquals(Set.of(TimeSlot.MORNING, TimeSlot.EVENING), prefs.getPreferredTimeSlots());
        assertEquals(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), prefs.getPreferredDays());

        verify(userRepository).save(user);
    }

    @Test
    void testSubmitQuestionnaire_updatesExistingPreferences() {
        UUID userId = UUID.randomUUID();
        LocalUser user = new LocalUser("testuser", "hash");
        LearningPreferences existing = new LearningPreferences(
                15, 60, 4, 5, 1,
                Set.of(TimeSlot.AFTERNOON),
                Set.of(DayOfWeek.TUESDAY)
        );
        user.setPreferences(existing);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setMinUnitDuration(20);
        dto.setMaxUnitDuration(80);
        dto.setMaxDayLoad(5);
        dto.setPreferredPauseDuration(8);
        dto.setTimeBeforeDeadlines(2);
        dto.setPreferredStudyTimes(Set.of(TimeSlot.MORNING));
        dto.setPreferredStudyDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));

        service.submitQuestionnaire(userId, dto);

        LearningPreferences updated = user.getPreferences();
        assertSame(existing, updated);
        assertEquals(20, updated.getMinUnitDurationMinutes());
        assertEquals(80, updated.getMaxUnitDurationMinutes());
        assertEquals(5, updated.getMaxDailyWorkloadHours());
        assertEquals(8, updated.getBreakDurationMinutes());
        assertEquals(2, updated.getDeadlineBufferDays());
        assertEquals(Set.of(TimeSlot.MORNING), updated.getPreferredTimeSlots());
        assertEquals(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), updated.getPreferredDays());

        verify(userRepository).save(user);
    }

    @Test
    void testSubmitQuestionnaire_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        QuestionnaireDTO dto = new QuestionnaireDTO();

        assertThrows(EntityNotFoundException.class,
                () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void testGetQuestionnaire_success() {
        // GIVEN: Ein Nutzer mit existierenden Lernpräferenzen
        UUID userId = UUID.randomUUID();
        LocalUser user = new LocalUser("testuser", "hash");

        LearningPreferences prefs = new LearningPreferences(
                30, 120, 8, 15, 3,
                Set.of(TimeSlot.MORNING, TimeSlot.AFTERNOON),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
        );
        user.setPreferences(prefs);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN: Die Methode aufgerufen wird
        QuestionnaireDTO result = service.getQuestionnaire(userId);

        // THEN: Das DTO muss die Werte der Entität korrekt widerspiegeln
        assertNotNull(result);
        assertEquals(30, result.getMinUnitDuration());
        assertEquals(120, result.getMaxUnitDuration());
        assertEquals(8, result.getMaxDayLoad());
        assertEquals(15, result.getPreferredPauseDuration());
        assertEquals(3, result.getTimeBeforeDeadlines());

        // Prüfung der Multiple-Choice Sets (die intern im DTO als Maps verwaltet werden)
        Set<TimeSlot> times = result.getPreferredStudyTimes();
        assertTrue(times.contains(TimeSlot.MORNING));
        assertTrue(times.contains(TimeSlot.AFTERNOON));
        assertEquals(2, times.size());

        Set<DayOfWeek> days = result.getPreferredStudyDays();
        assertTrue(days.contains(DayOfWeek.MONDAY));
        assertTrue(days.contains(DayOfWeek.FRIDAY));
        assertEquals(2, days.size());
    }

    @Test
    void testGetQuestionnaire_returnsEmptyDtoWhenNoPreferences() {
        // GIVEN: Ein Nutzer existiert, hat aber noch keine Präferenzen gesetzt
        UUID userId = UUID.randomUUID();
        LocalUser user = new LocalUser("testuser", "hash");
        user.setPreferences(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN: Die Methode aufgerufen wird
        QuestionnaireDTO result = service.getQuestionnaire(userId);

        // THEN: Es sollte ein leeres (nicht null) DTO zurückgegeben werden
        assertNotNull(result);
        // Da die Felder im DTO Maps sind, die initial leer sind,
        // sollten die Getter nun Exceptions werfen oder leere Sets liefern.
        assertTrue(result.getPreferredStudyDays().isEmpty());
        assertTrue(result.getPreferredStudyTimes().isEmpty());
    }

    @Test
    void testGetQuestionnaire_userNotFound() {
        // GIVEN: Die ID gehört zu keinem existierenden Nutzer
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN: Es muss eine EntityNotFoundException geworfen werden
        assertThrows(EntityNotFoundException.class, () -> service.getQuestionnaire(userId));
    }
}

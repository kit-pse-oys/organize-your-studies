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
        LocalUser user = new LocalUser("testuser", "hash", "salt");
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
        LocalUser user = new LocalUser("testuser", "hash", "salt");
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
    void testHasLearningPreferences_trueFalse() {
        UUID userId = UUID.randomUUID();

        LocalUser userWithPrefs = new LocalUser("testuser", "hash", "salt");
        userWithPrefs.setPreferences(new LearningPreferences(10, 60, 4, 5, 1, new HashSet<>(), new HashSet<>()));
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(userWithPrefs));
        assertTrue(service.hasLearningPreferences(userId));

        LocalUser userWithoutPrefs = new LocalUser("testuser2", "hash", "salt");
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(userWithoutPrefs));
        assertFalse(service.hasLearningPreferences(userId));

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.hasLearningPreferences(userId));
    }
}

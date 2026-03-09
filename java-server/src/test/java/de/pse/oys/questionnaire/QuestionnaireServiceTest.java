package de.pse.oys.questionnaire;

import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;
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

    private QuestionnaireDTO createValidDto() {
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setMinUnitDuration(30);
        dto.setMaxUnitDuration(90);
        dto.setMaxDayLoad(8);
        dto.setPreferredPauseDuration(15);
        dto.setTimeBeforeDeadlines(2);
        return dto;
    }

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
        dto.setPreferredPauseDuration(10);
        dto.setTimeBeforeDeadlines(2);
        dto.setPreferredStudyTimes(Set.of(TimeSlot.MORNING, TimeSlot.EVENING));
        dto.setPreferredStudyDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        service.submitQuestionnaire(userId, dto);

        LearningPreferences prefs = user.getPreferences();
        assertNotNull(prefs);
        assertEquals(30, prefs.getMinUnitDurationMinutes());
        assertEquals(90, prefs.getMaxUnitDurationMinutes());
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
        dto.setPreferredPauseDuration(8);
        dto.setTimeBeforeDeadlines(2);
        dto.setPreferredStudyTimes(Set.of(TimeSlot.MORNING));
        dto.setPreferredStudyDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));

        service.submitQuestionnaire(userId, dto);

        LearningPreferences updated = user.getPreferences();
        assertSame(existing, updated);
        assertEquals(20, updated.getMinUnitDurationMinutes());
        assertEquals(80, updated.getMaxUnitDurationMinutes());
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

    @Test
    void submitQuestionnaire_shouldThrowException_whenFieldIsNull() {
        // Arrange: LocalUser mit Pflichtfeldern erstellen
        LocalUser user = new LocalUser("testUser", "SecurePass123!");
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        QuestionnaireDTO dto = createValidDto();
        dto.setMinUnitDuration(null); // Triggert die Null-Prüfung

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void submitQuestionnaire_shouldThrowException_whenValuesAreZero() {
        LocalUser user = new LocalUser("testUser", "SecurePass123!");
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        QuestionnaireDTO dto = createValidDto();
        dto.setMaxDayLoad(0); // Triggert: maxDayLoad <= 0

        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void submitQuestionnaire_shouldThrowException_whenDayLoadExceeds24() {
        LocalUser user = new LocalUser("testUser", "SecurePass123!");
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        QuestionnaireDTO dto = createValidDto();
        dto.setMaxDayLoad(25); // Triggert: maxDayLoad > DAILY_HOURS_LIMITER

        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void submitQuestionnaire_shouldThrowException_whenMinGreaterThanMax() {
        LocalUser user = new LocalUser("testUser", "SecurePass123!");
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        QuestionnaireDTO dto = createValidDto();
        dto.setMinUnitDuration(100);
        dto.setMaxUnitDuration(50); // Triggert: minUnitDuration > maxUnitDuration

        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void submitQuestionnaire_shouldThrowException_whenDtoIsNull() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(userId, null));
    }
    @Test
    void submitQuestionnaire_shouldThrowException_whenNegativeValues() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setTimeBeforeDeadlines(-1); // Triggert deadlineBufferDays < 0

        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void testSubmitQuestionnaire_handlesNullSetsInDto() {
        UUID userId = UUID.randomUUID();
        LocalUser user = new LocalUser("testuser", "hash");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        QuestionnaireDTO dto = createValidDto();
        dto.setPreferredStudyTimes(null);
        dto.setPreferredStudyDays(null);

        service.submitQuestionnaire(userId, dto);

        assertNotNull(user.getPreferences().getPreferredTimeSlots());
        assertTrue(user.getPreferences().getPreferredTimeSlots().isEmpty());
        verify(userRepository).save(user);
    }

    @Test
    void validate_minUnitDuration_Zero_TrueHit() {
        UUID userId = UUID.randomUUID();
        // Mock user find
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setMinUnitDuration(0);

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_maxUnitDuration_Zero_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setMaxUnitDuration(0);

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_maxDayLoad_Zero_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setMaxDayLoad(0);

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_breakDuration_Negative_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setPreferredPauseDuration(-1);

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_deadlineBuffer_Negative_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setTimeBeforeDeadlines(-1);

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_maxUnitDuration_Null_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setMaxUnitDuration(null); // Triggert die 2. Bedingung

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_maxDayLoad_Null_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setMaxDayLoad(null); // Triggert die 3. Bedingung

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_breakDuration_Null_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setPreferredPauseDuration(null); // Triggert die 4. Bedingung

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }

    @Test
    void validate_deadlineBuffer_Null_TrueHit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        QuestionnaireDTO dto = createValidDto();
        dto.setTimeBeforeDeadlines(null); // Triggert die 5. Bedingung

        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(userId, dto));
    }
}

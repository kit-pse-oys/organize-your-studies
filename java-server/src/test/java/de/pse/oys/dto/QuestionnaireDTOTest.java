package de.pse.oys.dto;

import de.pse.oys.domain.enums.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für QuestionnaireDTO.
 * Testet nur die öffentlichen APIs mit einfachen und fokussierten Tests.
 */
class QuestionnaireDTOTest {

    private QuestionnaireDTO dto;

    @BeforeEach
    void setUp() {
        dto = new QuestionnaireDTO();
    }

    // ===== Single-Choice Field (MaxUnitDuration, MinUnitDuration, etc.) =====

    @Test
    void testSingleChoiceFields_SetAndGet() throws InvalidDtoException {
        dto.setMaxUnitDuration(60);
        assertEquals(60, dto.getMaxUnitDuration());

        dto.setMinUnitDuration(15);
        assertEquals(15, dto.getMinUnitDuration());
    }

    @Test
    void testSingleChoiceFields_Replace() throws InvalidDtoException {
        dto.setMaxUnitDuration(60);
        dto.setMaxUnitDuration(90);
        assertEquals(90, dto.getMaxUnitDuration());
    }

    @Test
    void testSingleChoiceFields_ThrowsInvalidDtoException() {
        assertThrows(InvalidDtoException.class, () -> {
            QuestionnaireDTO newDto = new QuestionnaireDTO();
            // Setzt mittels Reflection all-false Map für Exception
            setFieldViaReflection(newDto, "maxUnitDuration",
                new java.util.HashMap<>(Map.of(60, false, 90, false)));
            newDto.getMaxUnitDuration();
        });
    }

    @Test
    void testMaxDayLoad_DefaultValue() throws InvalidDtoException {
        assertEquals(24, dto.getMaxDayLoad());
    }

    @Test
    void testMaxDayLoad_CanBeOverridden() throws InvalidDtoException {
        dto.setMaxDayLoad(20);
        assertEquals(20, dto.getMaxDayLoad());
    }

    @Test
    void testTimeBeforeDeadlines_SetAndGet() throws InvalidDtoException {
        dto.setTimeBeforeDeadlines(7);
        assertEquals(7, dto.getTimeBeforeDeadlines());
    }

    @Test
    void testTimeBeforeDeadlines_ReturnsNullWhenNeverSet() {
        assertNull(dto.getTimeBeforeDeadlines());
    }

    @Test
    void testPreferredPauseDuration_SetAndGet() throws InvalidDtoException {
        dto.setPreferredPauseDuration(10);
        assertEquals(10, dto.getPreferredPauseDuration());
    }

    // ===== Multiple-Choice Field Tests =====

    @Test
    void testPreferredStudyTimes_SetAndGet() {
        Set<TimeSlot> times = new HashSet<>(Arrays.asList(TimeSlot.MORNING, TimeSlot.EVENING));
        dto.setPreferredStudyTimes(times);

        assertEquals(2, dto.getPreferredStudyTimes().size());
        assertTrue(dto.getPreferredStudyTimes().contains(TimeSlot.MORNING));
        assertTrue(dto.getPreferredStudyTimes().contains(TimeSlot.EVENING));
    }

    @Test
    void testPreferredStudyTimes_Replace() {
        dto.setPreferredStudyTimes(new HashSet<>(Arrays.asList(TimeSlot.MORNING)));
        dto.setPreferredStudyTimes(new HashSet<>(Arrays.asList(TimeSlot.EVENING)));

        assertTrue(dto.getPreferredStudyTimes().contains(TimeSlot.EVENING));
        assertFalse(dto.getPreferredStudyTimes().contains(TimeSlot.MORNING));
    }

    @Test
    void testPreferredStudyTimes_EmptyWhenNeverSet() {
        assertTrue(dto.getPreferredStudyTimes().isEmpty());
    }

    @Test
    void testPreferredStudyTimes_IgnoresNull() {
        dto.setPreferredStudyTimes(null);
        assertTrue(dto.getPreferredStudyTimes().isEmpty());
    }

    @Test
    void testPreferredStudyDays_SetAndGet() {
        Set<DayOfWeek> days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        dto.setPreferredStudyDays(days);

        assertEquals(2, dto.getPreferredStudyDays().size());
        assertTrue(dto.getPreferredStudyDays().contains(DayOfWeek.MONDAY));
        assertTrue(dto.getPreferredStudyDays().contains(DayOfWeek.FRIDAY));
    }

    @Test
    void testPreferredStudyDays_Replace() {
        dto.setPreferredStudyDays(new HashSet<>(Arrays.asList(DayOfWeek.MONDAY)));
        dto.setPreferredStudyDays(new HashSet<>(Arrays.asList(DayOfWeek.WEDNESDAY)));

        assertTrue(dto.getPreferredStudyDays().contains(DayOfWeek.WEDNESDAY));
        assertFalse(dto.getPreferredStudyDays().contains(DayOfWeek.MONDAY));
    }

    @Test
    void testPreferredStudyDays_EmptyWhenNeverSet() {
        assertTrue(dto.getPreferredStudyDays().isEmpty());
    }

    @Test
    void testPreferredStudyDays_IgnoresNull() {
        dto.setPreferredStudyDays(null);
        assertTrue(dto.getPreferredStudyDays().isEmpty());
    }

    // ===== Null-Safety =====

    @Test
    void testSetMaxUnitDuration_WithNullValue() {
        assertDoesNotThrow(() -> dto.setMaxUnitDuration(null));
    }

    @Test
    void testSetMinUnitDuration_WithNullValue() {
        assertDoesNotThrow(() -> dto.setMinUnitDuration(null));
    }

    @Test
    void testSetPreferredPauseDuration_WithNullValue() {
        assertDoesNotThrow(() -> dto.setPreferredPauseDuration(null));
    }

    @Test
    void testSetTimeBeforeDeadlines_WithNullValue() {
        assertDoesNotThrow(() -> dto.setTimeBeforeDeadlines(null));
    }

    @Test
    void testSetPreferredStudyTimes_WithNullMap() {
        setFieldViaReflection(dto, "preferredStudyTimes", null);
        assertDoesNotThrow(() -> dto.setPreferredStudyTimes(new HashSet<>(Arrays.asList(TimeSlot.MORNING))));
    }

    @Test
    void testSetPreferredStudyDays_WithNullMap() {
        setFieldViaReflection(dto, "preferredStudyDays", null);
        assertDoesNotThrow(() -> dto.setPreferredStudyDays(new HashSet<>(Arrays.asList(DayOfWeek.MONDAY))));
    }

    @Test
    void testSetMaxDayLoad_WithNullValue() {
        assertDoesNotThrow(() -> dto.setMaxDayLoad(null));
    }

    @Test
    void testSetMaxUnitDuration_InitializesMapWhenNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "maxUnitDuration", null);
        newDto.setMaxUnitDuration(60);
        assertEquals(60, newDto.getMaxUnitDuration());
    }

    @Test
    void testSetMinUnitDuration_InitializesMapWhenNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "minUnitDuration", null);
        newDto.setMinUnitDuration(15);
        assertEquals(15, newDto.getMinUnitDuration());
    }

    @Test
    void testSetMaxDayLoad_InitializesMapWhenNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "maxDayLoad", null);
        newDto.setMaxDayLoad(20);
        assertEquals(20, newDto.getMaxDayLoad());
    }

    @Test
    void testSetTimeBeforeDeadlines_InitializesMapWhenNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "timeBeforeDeadlines", null);
        newDto.setTimeBeforeDeadlines(7);
        assertEquals(7, newDto.getTimeBeforeDeadlines());
    }

    @Test
    void testSetPreferredPauseDuration_InitializesMapWhenNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "preferredPauseDuration", null);
        newDto.setPreferredPauseDuration(10);
        assertEquals(10, newDto.getPreferredPauseDuration());
    }

    @Test
    void testSetPreferredStudyTimes_InitializesEnumMapWhenNull() {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "preferredStudyTimes", null);
        Set<TimeSlot> times = new HashSet<>(Arrays.asList(TimeSlot.MORNING));
        newDto.setPreferredStudyTimes(times);
        assertTrue(newDto.getPreferredStudyTimes().contains(TimeSlot.MORNING));
    }

    @Test
    void testSetPreferredStudyDays_InitializesEnumMapWhenNull() {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        setFieldViaReflection(newDto, "preferredStudyDays", null);
        Set<DayOfWeek> days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY));
        newDto.setPreferredStudyDays(days);
        assertTrue(newDto.getPreferredStudyDays().contains(DayOfWeek.MONDAY));
    }

    @Test
    void testSetMaxUnitDuration_DoesNotReinitializeWhenNotNull() throws InvalidDtoException {
        // Setter mit null-Wert, Map wird NICHT neu initialisiert wenn sie bereits existiert
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        newDto.setMaxUnitDuration(60);
        newDto.setMaxUnitDuration(90);
        assertEquals(90, newDto.getMaxUnitDuration());
    }

    @Test
    void testSetMinUnitDuration_DoesNotReinitializeWhenNotNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        newDto.setMinUnitDuration(15);
        newDto.setMinUnitDuration(20);
        assertEquals(20, newDto.getMinUnitDuration());
    }

    @Test
    void testSetMaxDayLoad_DoesNotReinitializeWhenNotNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        newDto.setMaxDayLoad(20);
        newDto.setMaxDayLoad(16);
        assertEquals(16, newDto.getMaxDayLoad());
    }

    @Test
    void testSetTimeBeforeDeadlines_DoesNotReinitializeWhenNotNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        newDto.setTimeBeforeDeadlines(7);
        newDto.setTimeBeforeDeadlines(14);
        assertEquals(14, newDto.getTimeBeforeDeadlines());
    }

    @Test
    void testSetPreferredPauseDuration_DoesNotReinitializeWhenNotNull() throws InvalidDtoException {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        newDto.setPreferredPauseDuration(10);
        newDto.setPreferredPauseDuration(15);
        assertEquals(15, newDto.getPreferredPauseDuration());
    }

    @Test
    void testSetPreferredStudyTimes_DoesNotReinitializeWhenNotNull() {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        Set<TimeSlot> times1 = new HashSet<>(Arrays.asList(TimeSlot.MORNING));
        newDto.setPreferredStudyTimes(times1);

        Set<TimeSlot> times2 = new HashSet<>(Arrays.asList(TimeSlot.EVENING));
        newDto.setPreferredStudyTimes(times2);

        assertTrue(newDto.getPreferredStudyTimes().contains(TimeSlot.EVENING));
    }

    @Test
    void testSetPreferredStudyDays_DoesNotReinitializeWhenNotNull() {
        QuestionnaireDTO newDto = new QuestionnaireDTO();
        Set<DayOfWeek> days1 = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY));
        newDto.setPreferredStudyDays(days1);

        Set<DayOfWeek> days2 = new HashSet<>(Arrays.asList(DayOfWeek.WEDNESDAY));
        newDto.setPreferredStudyDays(days2);

        assertTrue(newDto.getPreferredStudyDays().contains(DayOfWeek.WEDNESDAY));
    }

    // ===== Hilfsmethoden =====

    private void setFieldViaReflection(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Reflection Fehler: " + e.getMessage(), e);
        }
    }
}





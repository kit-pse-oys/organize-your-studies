package de.pse.oys.domain;

import de.pse.oys.domain.enums.UnitStatus;
import de.pse.oys.dto.UnitDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Testklasse zur Maximierung der Coverage für die Klasse LearningUnit.
 * Konzentriert sich auf die Methoden hasPassed und toDTO.
 */
class LearningUnitTest {

    private Task mockTask;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        mockTask = Mockito.mock(Task.class);
        taskId = UUID.randomUUID();
        when(mockTask.getTaskId()).thenReturn(taskId);
    }

    // --- Tests für hasPassed() ---

    @Test
    void testHasPassed_EndTimeNull() {
        LearningUnit unit = new LearningUnit(mockTask, LocalDateTime.now(), null);

        assertFalse(unit.hasPassed(), "Sollte false zurückgeben, wenn endTime null ist.");
    }

    @Test
    void testHasPassed_Future() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        LearningUnit unit = new LearningUnit(mockTask, LocalDateTime.now(), future);

        assertFalse(unit.hasPassed(), "Sollte false zurückgeben, wenn die Zeit in der Zukunft liegt.");
        assertEquals(UnitStatus.PLANNED, unit.getStatus());
    }

    @Test
    void testHasPassed_PastAndPlanned() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);
        LearningUnit unit = new LearningUnit(mockTask, past.minusHours(1), past);

        // Der Aufruf sollte true zurückgeben und den Status auf COMPLETED setzen
        assertTrue(unit.hasPassed());
        assertEquals(UnitStatus.COMPLETED, unit.getStatus(), "Status sollte automatisch auf COMPLETED gesetzt werden.");
    }

    @Test
    void testHasPassed_PastButNotPlanned() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);
        LearningUnit unit = new LearningUnit(mockTask, past.minusHours(1), past);
        unit.markAsMissed(); // Status ist nun MISSED

        assertTrue(unit.hasPassed());
        assertEquals(UnitStatus.MISSED, unit.getStatus(), "Status sollte MISSED bleiben.");
    }

    // --- Tests für toDTO() ---

    @Test
    void testToDTO_FullData() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 12, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 12, 12, 0);
        LearningUnit unit = new LearningUnit(mockTask, start, end);

        UnitDTO dto = unit.toDTO();

        assertEquals(taskId, dto.getTask());
        assertEquals(LocalDate.of(2026, 3, 12), dto.getDate());
        assertEquals(LocalTime.of(10, 0), dto.getStart());
        assertEquals(LocalTime.of(12, 0), dto.getEnd());
    }

    @Test
    void testToDTO_NoTaskAndOnlyStart() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 15, 14, 0);
        LearningUnit unit = new LearningUnit(null, start, null);

        UnitDTO dto = unit.toDTO();

        assertNull(dto.getTask());
        assertEquals(LocalDate.of(2026, 3, 15), dto.getDate());
        assertEquals(LocalTime.of(14, 0), dto.getStart());
        assertNull(dto.getEnd());
    }

    @Test
    void testToDTO_OnlyEndSetsDate() {
        // Fall: startTime ist null, aber endTime ist vorhanden
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 18, 0);
        LearningUnit unit = new LearningUnit(mockTask, null, end);

        UnitDTO dto = unit.toDTO();

        assertNull(dto.getStart());
        assertEquals(LocalDate.of(2026, 5, 20), dto.getDate(), "Datum sollte von endTime übernommen werden, wenn startTime null ist.");
        assertEquals(LocalTime.of(18, 0), dto.getEnd());
    }

    @Test
    void testToDTO_EmptyUnit() {
        // Nutzt Reflection oder Hilfskonstruktor um ein leeres Objekt zu testen
        // In diesem Fall simulieren wir es durch manuelle null-Werte im Konstruktor
        LearningUnit unit = new LearningUnit(null, null, null);

        UnitDTO dto = unit.toDTO();

        assertNull(dto.getTask());
        assertNull(dto.getDate());
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
    }

    // --- Tests für die Bewertung (isRated) ---

    @Test
    void testIsRated_FalseThenTrue() {
        // 1. Initialer Zustand: Keine Bewertung vorhanden
        LearningUnit unit = new LearningUnit(mockTask, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        assertFalse(unit.isRated(), "isRated() sollte false sein, wenn keine Bewertung gesetzt wurde.");

        // 2. Zustand nach dem Setzen einer Bewertung
        UnitRating mockRating = Mockito.mock(UnitRating.class);
        unit.setRating(mockRating);

        assertTrue(unit.isRated(), "isRated() sollte true sein, nachdem eine Bewertung gesetzt wurde.");
    }
}
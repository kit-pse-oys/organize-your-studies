package de.pse.oys.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testklasse zur Maximierung der Coverage für RecurringFreeTime.
 * Deckt alle spezialisierten Methoden der Klasse ab.
 */
class RecurringFreeTimeTest {

    @Test
    void testConstructorAndGetter() {
        UUID userId = UUID.randomUUID();
        String title = "Wöchentliches Training";
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(20, 0);
        DayOfWeek day = DayOfWeek.MONDAY;

        RecurringFreeTime freeTime = new RecurringFreeTime(userId, title, start, end, day);

        assertEquals(userId, freeTime.getUserId());
        assertEquals(title, freeTime.getTitle());
        assertEquals(start, freeTime.getStartTime());
        assertEquals(end, freeTime.getEndTime());
        assertEquals(day, freeTime.getDayOfWeek());
    }

    @Test
    void testOccursOn_Match() {
        DayOfWeek day = DayOfWeek.WEDNESDAY;
        RecurringFreeTime freeTime = new RecurringFreeTime(UUID.randomUUID(), "T", LocalTime.NOON, LocalTime.MIDNIGHT, day);

        // Mittwoch prüfen
        LocalDate wednesday = LocalDate.of(2026, 3, 11);
        assertTrue(freeTime.occursOn(wednesday), "Sollte an einem Mittwoch wahr sein.");
    }

    @Test
    void testOccursOn_NoMatch() {
        DayOfWeek day = DayOfWeek.MONDAY;
        RecurringFreeTime freeTime = new RecurringFreeTime(UUID.randomUUID(), "T", LocalTime.NOON, LocalTime.MIDNIGHT, day);

        // Dienstag prüfen
        LocalDate tuesday = LocalDate.of(2026, 3, 10);
        assertFalse(freeTime.occursOn(tuesday), "Sollte an einem Dienstag falsch sein.");
    }

    @Test
    void testOccursOn_NullDate() {
        RecurringFreeTime freeTime = new RecurringFreeTime();
        assertFalse(freeTime.occursOn(null), "Sollte bei null-Datum false zurückgeben.");
    }

    @Test
    void testGetRepresentativeDate_Mapping() {
        // Testet die deterministische Kodierung: Monday = 1970-01-05
        RecurringFreeTime freeTime = new RecurringFreeTime(UUID.randomUUID(), "T", LocalTime.NOON, LocalTime.MIDNIGHT, DayOfWeek.MONDAY);
        assertEquals(LocalDate.of(1970, 1, 5), freeTime.getRepresentativeDate());

        // Testet Sonntag (plus 6 Tage)
        freeTime = new RecurringFreeTime(UUID.randomUUID(), "T", LocalTime.NOON, LocalTime.MIDNIGHT, DayOfWeek.SUNDAY);
        assertEquals(LocalDate.of(1970, 1, 11), freeTime.getRepresentativeDate());
    }

    @Test
    void testGetRepresentativeDate_Null() {
        RecurringFreeTime freeTime = new RecurringFreeTime(); // dayOfWeek ist null
        assertNull(freeTime.getRepresentativeDate());
    }

    @Test
    void testApplyDtoDate_ValidDate() {
        RecurringFreeTime freeTime = new RecurringFreeTime();
        LocalDate friday = LocalDate.of(2026, 3, 13);

        freeTime.applyDtoDate(friday);

        assertEquals(DayOfWeek.FRIDAY, freeTime.getDayOfWeek());
    }

    @Test
    void testApplyDtoDate_Null() {
        RecurringFreeTime freeTime = new RecurringFreeTime(UUID.randomUUID(), "T", LocalTime.NOON, LocalTime.MIDNIGHT, DayOfWeek.FRIDAY);

        freeTime.applyDtoDate(null);

        assertNull(freeTime.getDayOfWeek(), "dayOfWeek sollte null sein, wenn das Datum null ist.");
    }
}
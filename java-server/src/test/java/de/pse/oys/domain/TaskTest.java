package de.pse.oys.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Konsolidierte Testklasse für die Geschäftslogik der Task-Domänenobjekte.
 * Deckt ExamTask, OtherTask, SubmissionTask und die Basisklasse Task ab.
 */
class TaskTest {

    // --- Tests für ExamTask ---

    @Test
    void testExamTask_HardDeadlineAndActive() {
        LocalDate examDate = LocalDate.now().plusDays(5);
        ExamTask task = new ExamTask("Mathe Prüfung", 120, examDate);

        // Test getHardDeadline: Muss 00:00 Uhr am Prüfungstag sein
        assertEquals(examDate.atStartOfDay(), task.getHardDeadline());

        // Test isActive: Vor der Prüfung aktiv
        assertTrue(task.isActive());

        // Test isActive: Nach der Prüfung inaktiv
        task.setExamDate(LocalDate.now().minusDays(1));
        assertFalse(task.isActive());

        // Test null-Check
        task.setExamDate(null);
        assertNull(task.getHardDeadline());
        assertFalse(task.isActive());
    }

    // --- Tests für OtherTask ---

    @Test
    void testOtherTask_Logic() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        OtherTask task = new OtherTask("Projektarbeit", 300, start, end);

        // Test getHardDeadline: Entspricht endTime
        assertEquals(end, task.getHardDeadline());

        // Test isActive: Innerhalb des Zeitrahmens
        assertTrue(task.isActive());

        // Test isActive: Außerhalb des Zeitrahmens (Zukunft)
        task.setStartTime(LocalDateTime.now().plusDays(1));
        assertFalse(task.isActive());

        // Test null-Checks
        task.setStartTime(null);
        assertFalse(task.isActive());
    }

    @Test
    void testOtherTask_IsActive_BranchCoverage() {
        LocalDateTime now = LocalDateTime.now();

        // Fall: Zeitfenster aktiv
        OtherTask task = new OtherTask("Aktiv", 60, now.minusHours(1), now.plusHours(1));
        assertTrue(task.isActive());

        // False Hit: Jetzt ist NACH dem Ende
        task.setEndTime(now.minusMinutes(1));
        assertFalse(task.isActive(), "Sollte false sein, wenn die aktuelle Zeit nach endTime liegt.");

        // False Hit: Endzeit ist null
        task.setEndTime(null);
        assertFalse(task.isActive(), "Sollte false sein, wenn endTime null ist.");

        // False Hit: Startzeit ist null
        task.setStartTime(null);
        task.setEndTime(now.plusHours(1));
        assertFalse(task.isActive(), "Sollte false sein, wenn startTime null ist.");
    }

    // --- Tests für SubmissionTask (Wiederkehrende Deadlines) ---

    @Test
    void testSubmissionTask_ComplexDeadlineLogic() {
        LocalDateTime anchor = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().plusWeeks(3);
        // Zyklus: Alle 1 Woche
        SubmissionTask task = new SubmissionTask("Wöchentliche Abgabe", 60, anchor, 1, end);

        // 1. Nächste Deadline berechnen: Sollte anchor + 1 Woche sein
        LocalDateTime next = task.getHardDeadline();
        assertEquals(anchor.plusWeeks(1), next);

        // 2. Vor der ersten Deadline: Erste Deadline ist die nächste
        task.setFirstDeadline(LocalDateTime.now().plusDays(1));
        assertEquals(task.getFirstDeadline(), task.getHardDeadline());

        // 3. Nach der endTime: Keine Deadline mehr
        task.setEndTime(LocalDateTime.now().minusMinutes(1));
        assertNull(task.getHardDeadline());
        assertFalse(task.isActive());

        // 4. Null-Werte in der Berechnung
        task.setEndTime(null);
        assertNull(task.getHardDeadline());
    }

    @Test
    void testSubmissionTask_IsActive_FalseHits() {
        // False Hit: Endzeit ist null
        SubmissionTask task = new SubmissionTask("Kein Ende", 30, LocalDateTime.now(), 1, null);
        assertFalse(task.isActive(), "Sollte false sein, wenn endTime null ist.");

        // False Hit: Jetzt ist nach dem Ende
        task.setEndTime(LocalDateTime.now().minusDays(1));
        assertFalse(task.isActive(), "Sollte false sein, wenn die Aufgabe bereits abgelaufen ist.");
    }

    @Test
    void testSubmissionTask_ComputeNextDeadline_FullCoverage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime anchor = now.plusDays(2);
        LocalDateTime end = now.plusDays(10);

        // 1. Branch: null-Werte
        SubmissionTask task = new SubmissionTask("Null-Check", 60, null, 1, end);
        assertNull(task.getHardDeadline(), "Sollte null liefern, wenn firstDeadline fehlt.");

        // 2. Branch: Jetzt >= endTime
        task = new SubmissionTask("Vorbei", 60, now.minusDays(5), 1, now.minusMinutes(1));
        assertNull(task.getHardDeadline(), "Sollte null liefern, wenn from >= endTime.");

        // 3. Branch: Jetzt <= firstDeadline ABER firstDeadline > endTime
        // (Szenario: Eine Aufgabe, deren einzige Deadline nach ihrem offiziellen Ende liegt)
        task = new SubmissionTask("Logikfehler", 60, now.plusDays(5), 1, now.plusDays(2));
        assertNull(task.getHardDeadline(), "Sollte null liefern, wenn die erste Deadline nach dem Ende liegt.");

        // 4. Branch: Jetzt <= firstDeadline UND firstDeadline <= endTime
        task = new SubmissionTask("Zukunft", 60, anchor, 1, end);
        assertEquals(anchor, task.getHardDeadline(), "Sollte den Anker liefern, wenn wir noch davor sind.");

        // 5. Branch: Zyklus-Berechnung (Candidate > endTime)
        // Wir sind nach dem Anker, der nächste Zyklus wäre aber nach dem Ende
        anchor = now.minusDays(1);
        end = now.plusDays(2);
        task = new SubmissionTask("Letzter Zyklus", 60, anchor, 1, end); // Zyklus 1 Woche
        // Nächste Deadline wäre anchor + 7 Tage = now + 6 Tage. Das ist > end (now + 2).
        assertNull(task.getHardDeadline(), "Sollte null liefern, wenn der nächste berechnete Zyklus nach dem Ende liegt.");
    }

    // --- Tests für die Basisklasse Task (Logik-Vererbung) ---

    @Test
    void testBaseTask_SoftDeadline() {
        // Wir nutzen OtherTask als konkrete Implementierung zum Testen der Basislogik
        LocalDateTime hard = LocalDateTime.now().plusDays(10);
        OtherTask task = new OtherTask("Puffertest", 0, null, hard);

        // Test getSoftDeadline: 3 Tage Puffer
        LocalDateTime soft = task.getSoftDeadline(3);
        assertEquals(hard.minusDays(3), soft);

        // Test ohne harte Deadline
        task.setEndTime(null);
        assertNull(task.getSoftDeadline(3));
    }

    @Test
    void testBaseTask_LearningUnitHandling() {
        OtherTask task = new OtherTask("Unit-Test", 60, null, null);
        LearningUnit unit = new LearningUnit(task, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // Test: Lazy Initialization der Liste
        assertNotNull(task.getLearningUnits());
        assertEquals(0, task.getLearningUnits().size());

        // Test: Hinzufügen einer Einheit
        task.addLearningUnit(unit);
        assertEquals(1, task.getLearningUnits().size());

        // Test: Duplikatsvermeidung
        task.addLearningUnit(unit);
        assertEquals(1, task.getLearningUnits().size(), "Einheit darf nicht doppelt hinzugefügt werden.");
    }

    @Test
    void testSubmissionTask_CycleNormalization() {
        // Testet, ob cycleWeeks auf mindestens 1 gesetzt wird
        SubmissionTask task = new SubmissionTask("MinCycle", 10, null, 0, null);
        assertEquals(1, task.getCycleWeeks());

        task.setCycleWeeks(-5);
        assertEquals(1, task.getCycleWeeks());
    }


}
package de.pse.oys;

import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.TimeSlot;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * TestUserFactory – Erstellt Testdomänen für die Testfälle.
 * Diese Klasse wurde im weiteren Verlauf der Qualitätssicherung im Zuge der Maximierung der Coverage eingefügt um sich
 * wiederholende Testdaten zu sparen, wenn es nicht um die konkrete Konfiguration dieser geht, sie aber bennötigt wird um entsprechende
 * Testfälle (bzgl. der Coverage) zu erstellen.
 *
 * @author uhupo
 * @version 1.0
 */
public class TestUserFactory {

    /**
     * Erstellt einen User mit vollständig initialisierten Lernpräferenzen unter Verwendung
     * des parametrisierten Konstruktors von LearningPreferences.
     * @return ein LocalUser mit validen Standard-Präferenzen für Planungstests
     */
    public static LocalUser createLocalUserWithPrefs() {
        LocalUser user = new LocalUser("testuser", "testpassword");

        // Vorbereitung der Sets für den Konstruktor
        Set<DayOfWeek> allDays = new java.util.HashSet<>(java.util.Arrays.asList(DayOfWeek.values()));
        Set<TimeSlot> allSlots = new java.util.HashSet<>(java.util.Arrays.asList(TimeSlot.values()));

        // Nutzt den großen Konstruktor: minDuration, maxDuration, maxWorkload, break, buffer, slots, days
        LearningPreferences prefs = new LearningPreferences(
                45,         // minUnitDurationMinutes
                90,         // maxUnitDurationMinutes
                8,          // maxDailyWorkloadHours
                15,         // breakDurationMinutes
                2,          // deadlineBufferDays
                allSlots,   // preferredTimeSlots
                allDays     // preferredDays
        );

        user.setPreferences(prefs);

        return user;
    }
}

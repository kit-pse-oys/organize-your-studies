package de.pse.oys;

import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.TimeSlot;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * TestDomainFactory – Erstellt Testdomänen für die Testfälle.
 * Diese Klasse wurde im weiteren Verlauf der Qualitätssicherung im Zuge der Maximierung der Coverage eingefügt um sich
 * wiederholende Testdaten zu sparen, wenn es nicht um die konkrete Konfiguration dieser geht, sie aber bennötigt wird um entsprechende
 * Testfälle (bzgl. der Coverage) zu erstellen.
 *
 * @author uhupo
 * @version 1.0
 */
public class TestDomainFactory {

    /**
     * Erstellt einen neuen spezifizierbaren LocalUser mit den angegebenen Anmeldedaten.
     * @param username der gewünschte Benutzername
     * @param password das gewünschte Passwort
     * @return ein LocalUser mit den angegebenen Anmeldedaten
     */
    public static LocalUser createLocalUser(String username, String password) {
        return new LocalUser(username, password);
    }

    /**
     * Erstellt einen neuen LocalUser mit Standard-Anmeldedaten für Testzwecke.
     * @return ein LocalUser mit vordefiniertem Benutzernamen und Passwort
     */
    public static LocalUser createLocalUser() {
        return new LocalUser("testuser", "testpassword");
    }

    /**
     * Erstellt einen User mit vollständig initialisierten Lernpräferenzen unter Verwendung
     * des parametrisierten Konstruktors von LearningPreferences.
     * * @return ein LocalUser mit validen Standard-Präferenzen für Planungstests
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

    /**
     * Erstellt ein Modul mit spezifischen Attributen und ordnet es einem Benutzer zu.
     * @param title der Titel des Moduls
     * @param priority die Priorität des Moduls
     * @param user der Besitzer des Moduls
     * @return ein konfiguriertes Modul-Objekt
     */
    public static Module createModule(String title, ModulePriority priority, User user) {
        Module module = new Module(title, priority);
        module.setUser(user);
        return module;
    }

    /**
     * Erstellt ein Modul mit Standardwerten für einen gegebenen Benutzer.
     * @param user der Besitzer des Moduls
     * @return ein Modul mit dem Titel "Testmodul" und Priorität MEDIUM
     */
    public static Module createModule(User user) {
        return createModule("Testmodul", ModulePriority.MEDIUM, user);
    }
}

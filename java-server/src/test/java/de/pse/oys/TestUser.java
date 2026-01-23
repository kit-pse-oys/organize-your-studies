package de.pse.oys;

import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.UserType;

import java.util.UUID;

/**
 * TestUser – Testklasse für User-Objekte im Testkontext.
 * Repliziert die Struktur der Haupt-User-Klasse für Testzwecke ohne die Abstraktion der User-Klasse zu verletzen.
 *
 * @author uhupo
 * @version 1.0
 */
public class TestUser extends User {

    /**
     * Konstruktor für TestUser.
     * Nutzt den Konstruktor der Basisklasse User.
     * @param userId die UUID des Nutzers
     * @param username der Nutzername
     * @param type der Typ des Nutzers (irrelevant für die Tests, wo diese Klasse verwendet wird)
     */
    public TestUser(UUID userId, String username, UserType type) {
        super(userId, username, type);
    }
}

package de.pse.oys.domain;

import de.pse.oys.domain.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.UUID;

/**
 * Repräsentiert einen Nutzer, der sich lokal über Benutzername und Passwort authentifiziert.
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("local")
public class LocalUser extends User {

    /** * Kryptografischer Hash des Passworts.
     * Das Feld ist als nicht updatable markiert, um den readOnly-Status aus dem Entwurf zu wahren.
     */
    @Column(name = "password_hash", updatable = false)
    private String passwordHash;

    /** * Nutzerabhängige Zufallsvariable für das Hashing (Salt).
     * Auch dieses Feld kann nach der Erstellung nicht mehr geändert werden.
     */
    @Column(name = "password_salt", updatable = false)
    private String passwordSalt;

    /**
     * Standardkonstruktor ohne Argumente für JPA/Hibernate.
     * Ohne das Schlüsselwort 'final' bei den Feldern lässt sich dieser nun problemlos kompilieren.
     */
    protected LocalUser() {
        super();
    }

    /**
     * Erzeugt eine Instanz für die lokale Authentifizierung.
     * @param userId Eindeutige ID des Nutzers.
     * @param username Der gewählte Benutzername.
     * @param passHash Der bereits berechnete Passwort-Hash.
     * @param salt Das verwendete Salt.
     */
    public LocalUser(UUID userId, String username, String passHash, String salt) {
        super(userId, username, UserType.LOCAL);
        this.passwordHash = passHash;
        this.passwordSalt = salt;
    }

    // Getter Methoden


    /**
     * @return den Salt-Wert des Passwort-Hashings.
     */
    public String getSalt() {
        return passwordSalt;
    }

    /**
     * @return den Passwort-Hash.
     */
    public String getHashedPassword() {
        return passwordHash;
    }
}
package de.pse.oys.domain;

import de.pse.oys.domain.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Repräsentiert einen Nutzer, der sich lokal über Benutzername und Passwort authentifiziert.
 *
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

    /**
     * Standardkonstruktor ohne Argumente für JPA/Hibernate.
     * Ohne das Schlüsselwort 'final' bei den Feldern lässt sich dieser nun problemlos kompilieren.
     */
    protected LocalUser() {
        super();
    }

    /**
     * Erzeugt eine Instanz für die lokale Authentifizierung.
     *
     * @param username Der gewählte Benutzername.
     * @param passHash Der bereits berechnete Passwort-Hash.
     */
    public LocalUser(String username, String passHash) {
        super(username, UserType.LOCAL);
        this.passwordHash = passHash;
    }

    // Getter Methoden

    /**
     * @return den Passwort-Hash.
     */
    public String getHashedPassword() {
        return passwordHash;
    }
}
package de.pse.oys.domain;

import de.pse.oys.domain.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Repräsentiert einen Nutzer, der über einen externen Identity Provider (OIDC) verwaltet wird.
 *
 * @author utgid
 * @version 1.0
 */
@Entity
@DiscriminatorValue("google")
public class ExternalUser extends User {

    /**
     * Technische Subject-ID des externen Anbieters.
     * updatable = false stellt sicher, dass die ID nach der Erstellung nicht mehr geändert wird.
     */
    @Column(name = "google_sub", updatable = false)
    private String externalSubjectId;

    /** No-Args-Constructor für Hibernate. */
    protected ExternalUser() {
        super();
    }

    /**
     * Spezialisierter Konstruktor für externe OIDC-Nutzer[cite: 800, 832].
     *
     * @param username der Anzeigename des Nutzers
     * @param extId    die Subject-ID des externen Identity Providers
     * @param userType der Benutzertyp, referenziert den Authentifizierungsanbieter des Benutzers
     */
    public ExternalUser(String username, String extId, UserType userType) {
        super(username, userType);
        this.externalSubjectId = extId;
    }

    /**
     * Gibt die Subject-ID des externen Identity Providers zurück[cite: 801, 823].
     * @return die externe Subject-ID
     */
    public String getExternalSubjectId() {
        return externalSubjectId;
    }
}
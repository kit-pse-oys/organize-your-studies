package de.pse.oys.domain;

import de.pse.oys.domain.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.UUID;

/**
 * Repräsentiert einen Nutzer, der über einen externen Identity Provider (OIDC) verwaltet wird.
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
     */
    public ExternalUser(UUID userId, String username, String extId) {
        super(userId, username, UserType.GOOGLE);
        this.externalSubjectId = extId;
    }

    /**
     * Gibt die Subject-ID des externen Identity Providers zurück[cite: 801, 823].
     */
    public String getExternalIdentifier() {
        return externalSubjectId;
    }
}
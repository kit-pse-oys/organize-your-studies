package de.pse.oys.dto.auth;

/**
 * AuthType – Enum für die verschiedenen Arten der Authentifizierung, die unterstützt werden.
 * Es wird verwendet, um zwischen lokaler Authentifizierung und Authentifizierung
 * über einen externen Identity Provider zu unterscheiden.
 *
 *
 *
 * @author uhupo
 * @version 1.0
 */
public enum AuthType {
    /**
     * BASIC - Ein Nutzer versucht sich mit lokalem Benutzernamen und Passwort zu authentifizieren.
     */
    BASIC,

    /**
     * OIDC - Ein Nutzer versucht sich über einen externen OpenID Connect (OIDC) Identity Provider zu authentifizieren.
     */
    OIDC
}

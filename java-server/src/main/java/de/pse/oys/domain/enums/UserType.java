package de.pse.oys.domain.enums;

/**
 * Definiert die Art der Authentifizierung für einen Benutzer.
 * Dieses Enum wird im Domänenmodell verwendet, um zwischen lokalen
 * Konten und Konten von externen Identitätsanbietern zu unterscheiden.
 * <p>
 * Um bei dem Authentifizierungsprozess den entsprechenden Mechanismus zu wählen,
 * wird dieses Enum auch als Diskriminator bei der UserType-Auswahl verwendet.
 * @see de.pse.oys.dto.auth.LoginDTO
 *
 * @author utgid
 * @version 1.0
 */
public enum UserType {

    /**
     * Kennzeichnet ein lokales Benutzerkonto, das mit Benutzername
     * und einem Passwort-Hash in der eigenen Datenbank gespeichert ist.
     */
    LOCAL,

    /**
     * Kennzeichnet ein Konto, das über einen externen Identity Provider
     * (z. B. Google via OIDC) authentifiziert wird.
     */
    GOOGLE
}
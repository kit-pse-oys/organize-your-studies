package de.pse.oys.model;

/**
 * Definiert die Art der Authentifizierung für einen Benutzer.
 * Dieses Enum wird im Domänenmodell verwendet, um zwischen lokalen
 * Konten und Konten von externen Identitätsanbietern zu unterscheiden.
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
    AUTH
}
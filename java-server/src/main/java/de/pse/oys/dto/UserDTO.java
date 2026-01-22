package de.pse.oys.dto;

/**
 * Datentransferobjekt für Benutzerdaten.
 * Wird primär für die Registrierung und Validierung von Benutzerkonten verwendet.
 * @author utgid
 * @version 1.0
 */
public class UserDTO {

    /** Die eindeutige Kennung des Nutzers als String. */
    private String id;

    /** Der gewählte Nutzername des Benutzers. */
    private String username;

    /** Das Passwort des Benutzers (im Klartext bei Request, verschlüsselt in der Domain). */
    private String password;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public UserDTO() {
    }

    // Getter

    /** @return Die ID des Nutzers. */
    public String getId() { return id; }

    /** @return Der Nutzername. */
    public String getUsername() { return username; }

    /** @return Das Passwort. */
    public String getPassword() { return password; }

    // Setter

    /** @param id Die zu setzende Nutzer-ID. */
    public void setId(String id) { this.id = id; }

    /** @param username Der zu setzende Nutzername. */
    public void setUsername(String username) { this.username = username; }

    /** @param password Das zu setzende Passwort. */
    public void setPassword(String password) { this.password = password; }
}
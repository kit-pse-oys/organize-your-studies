package de.pse.oys.dto;

/**
 * UserDTO –
 *
 * @author uhupo
 * @version 1.0
 */
/**
 * Data Transfer Object für Benutzer.
 * Wird für Registrierung und Profilanzeige verwendet.
 */
public class UserDTO {
    private String id;
    private String username;
    private String password;


    /**
     * Gibt die eindeutige ID des Nutzers zurück.
     * @return die ID des Nutzers
     */
    public String getId() {
        return id;
    }

    /**
     * Gibt den Nutzernamen zurück.
     * @return der Nutzername
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gibt das Passwort zurück.
     * @return das Passwort
     */
    public String getPassword() {
        return password;
    }
}

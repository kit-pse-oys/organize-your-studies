package de.pse.oys.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

/**
 * UserPrincipal implementiert die UserDetails-Schnittstelle von Spring Security.
 * Diese Klasse dient als Sicherheits-Container, der die Identität des authentifizierten
 * Benutzers innerhalb der Anwendung repräsentiert.
 * Im Gegensatz zum Standard-Principal speichert diese Klasse die {@link UUID} des Benutzers,
 * um eine eindeutige Zuordnung in der Datenbank ohne erneute Abfragen zu ermöglichen.
 */
public class UserPrincipal implements UserDetails {
    private final UUID userId;
    private final String username;
    private final String password;

    public UserPrincipal(UUID userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    /**
     * Gibt die eindeutige Identifikationsnummer (UUID) des Benutzers zurück.
     * Diese ID wird aus dem JWT extrahiert und dient als Primärschlüssel für alle
     * datenbankbezogenen Operationen in den Services.
     * @return die UUID des authentifizierten Benutzers.
     */
    public UUID getUserId() {
        return userId;
    }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return null; }

    // Weitere notwendige Overrides für UserDetails...
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
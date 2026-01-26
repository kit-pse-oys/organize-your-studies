package de.pse.oys.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

public class UserPrincipal implements UserDetails {
    private final UUID userId;
    private final String username;
    private final String password;

    public UserPrincipal(UUID userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    // Das ist die Methode, die dir im Controller gefehlt hat!
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
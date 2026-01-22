package de.pse.oys.service.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityConfig – Konfigurationsklasse für Sicherheitsaspekte wie Passwortverschlüsselung
 * Ermöglicht die Bereitstellung eines PasswordEncoders als Bean im Start der Anwendung.
 *
 * @author uhupo
 * @version 1.0
 */
@Configuration
public class SecurityConfig {

    /**
     * Erstellt und gibt einen PasswordEncoder zurück, der BCrypt zur Passwortverschlüsselung verwendet.
     *
     * @return die PasswordEncoder-Instanz
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

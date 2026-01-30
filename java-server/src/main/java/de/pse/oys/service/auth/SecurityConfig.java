package de.pse.oys.service.auth;

import de.pse.oys.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig – Konfigurationsklasse für Sicherheitsaspekte.
 * Konfiguriert die HTTP-Sicherheit, die zustandslose Sitzungsverwaltung (JWT)
 * und den Passwort-Encoder.
 *
 * @author uhupo, utgid
 * @version 1.1
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    /**
     * Konstruktor mit Dependency Injection.
     * @param jwtFilter der JWT-Filter zur Validierung von Tokens und extrahierung der Benutzer-UUID
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Konfiguriert die zentrale Sicherheitsfilterkette.
     * Der {@link JwtFilter} wird explizit vor dem {@link UsernamePasswordAuthenticationFilter}
     * platziert, damit die Authentifizierung via JWT als primärer Identitätsnachweis
     * Vorrang erhält. Dies stellt sicher, dass die Benutzer-UUID bereits aus dem Token
     * extrahiert und im SecurityContext hinterlegt wurde, bevor die Standard-Prüfmechanismen
     * von Spring Security greifen oder den Request mangels Session/Anmeldedaten abweisen.
     *
     * @param http das HttpSecurity-Objekt zur Konfiguration
     * @return die konfigurierte SecurityFilterChain
     * @throws Exception bei Konfigurationsfehlern
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deaktivieren, da wir zustandslose JWTs verwenden
                .csrf(AbstractHttpConfigurer::disable)

                // Session-Management auf STATELESS setzen (keine JSESSIONID)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Berechtigungen festlegen
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/api/users/register").permitAll() // Login/Register sind öffentlich
                        .anyRequest().authenticated()            // Alles andere erfordert einen Token
                );


        // Der JwtFilter wird vor dem UsernamePasswordAuthenticationFilter eingefügt,
        // damit JWT-Token bereits vor der Standard-Authentifizierung geprüft werden.
        // So kann die Benutzer-Identität aus dem Token extrahiert und im SecurityContext
        // hinterlegt werden, bevor Spring Security eine sessionbasierte Authentifizierung versucht.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


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

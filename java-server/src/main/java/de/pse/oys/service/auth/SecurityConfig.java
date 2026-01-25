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

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Definiert die Sicherheitsfilterkette.
     * Hier wird festgelegt, dass die API zustandslos ist und der JwtFilter
     * vor dem Standard-Authentifizierungsfilter ausgeführt wird.
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
                        .requestMatchers("/auth/**").permitAll() // Login/Register sind öffentlich
                        .anyRequest().authenticated()            // Alles andere erfordert einen Token
                );

        // HIER WIRD DER FILTER EINGEHÄNGT:
        // Unser JwtFilter soll VOR dem UsernamePasswordAuthenticationFilter laufen.
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

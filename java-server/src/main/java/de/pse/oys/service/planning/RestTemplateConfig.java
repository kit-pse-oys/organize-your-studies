package de.pse.oys.service.planning;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateConfig – Konfiguriert eine RestTemplate-Instanz für HTTP-Anfragen.
 * Dies wird benötigt um den Microservice für die Lernplanberechnung anzusprechen.
 * Eine extra Konfigurationsklasse ist notwendig, damit Spring die Bean verwalten kann.
 *
 * @author uhupo
 * @version 1.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Erstellt und konfiguriert eine RestTemplate-Instanz für HTTP-Anfragen.
     * @return Eine neue RestTemplate-Instanz.
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}

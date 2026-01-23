package de.pse.oys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main – Die Hauptklasse der Spring Boot Anwendung.
 * Diese Klasse enthält die main-Methode zum Starten der Anwendung.
 *
 * @author uhupo
 * @version 1.0
 */

@SpringBootApplication
public class Main {
    /**
     * Hauptmethode zum Starten der Spring Boot Anwendung.
     * @param args Kommandozeilenargumente (ignoriert)
     */
    public static void main(String[] args) {
        System.out.printf("Spring Boot Application started with args: %s%n", String.join(", ", args));
        SpringApplication.run(Main.class, args);
    }
}
package de.pse.oys.domain;

import de.pse.oys.domain.enums.UserType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Abstrakte Basisklasse für alle Nutzer im System.
 * Implementiert die zentralen Identitätsmerkmale und die Verwaltung von
 * Beziehungen zu Modulen, Freizeiten und Lernplänen.
 * @author utgid
 * @version 1.0
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "auth_provider")
public abstract class User {

    /**
     * Eindeutige Kennung des Nutzers.
     * updatable = false stellt sicher, dass die ID nach der Erstellung nicht mehr geändert wird.
     */
    @Id
    @GeneratedValue
    @Column(name = "user_id", updatable = false)
    private UUID userId;

    /** * Anzeigename des Benutzers.
     * Auch dieser ist laut Entwurf nach der Erstellung schreibgeschützt.
     */
    @Column(name = "username", updatable = false)
    private String username;

    /** Sicherheits-Hash des aktuell gültigen Refresh-Tokens. */
    private String refreshTokenHash;

    /** Zeitpunkt, an dem der Refresh-Token seine Gültigkeit verliert. */
    private LocalDateTime refreshTokenExpiration;

    /** Art des Benutzerkontos (LOCAL oder AUTH). */
    @Enumerated(EnumType.STRING)
    private UserType userType;

    /** Verknüpfte Lernpräferenzen des Nutzers. */
    @OneToOne(cascade = CascadeType.ALL)
    private LearningPreferences preferences;

    /** Liste der dem Nutzer zugeordneten Studienmodule. */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Module> modules;

    /** Liste der definierten Freizeiten und Zeitrestriktionen. */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<FreeTime> freeTimes;

    /** Liste der generierten wochenbasierten Lernpläne. */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<LearningPlan> learningPlans;

    /**
     * Standardkonstruktor ohne Argumente für JPA/Hibernate.
     * Ermöglicht die Instanziierung durch das Framework trotz fehlender final-Felder.
     */
    protected User() {
    }

    /**
     * Basiskonstruktor zur Initialisierung eines Nutzers.
     *
     * @param username Benutzername.
     * @param type     Typ des Accounts.
     */
    protected User(String username, UserType type) {
        this.username = username;
        this.userType = type;
    }


    /**
     * Instanziiert einen neuen Lernplan für den definierten Zeitraum und
     * verknüpft diesen mit dem Nutzerprofil[cite: 807, 808].
     * @param start Startdatum
     * @param end Enddatum
     */
    public void createNewLearningPlan(LocalDate start, LocalDate end) {
        LearningPlan plan = new LearningPlan(start, end);
        this.learningPlans.add(plan);
    }

    /**
     * Überprüft, ob der aktuell gespeicherte Hash des Refresh-Tokens noch gültig ist[cite: 810, 811].
     * @return true, wenn der Token noch nicht abgelaufen ist.
     */
    public boolean isRefreshTokenValid() {
        return refreshTokenHash != null &&
                refreshTokenExpiration != null &&
                refreshTokenExpiration.isAfter(LocalDateTime.now());
    }

    /**
     * Aktualisiert den Sicherheits-Hash des Refresh-Tokens und setzt das Ablaufdatum neu[cite: 812, 813].
     * @param newHash Neuer Hash-Wert.
     * @param durationDays Gültigkeitsdauer in Tagen.
     */
    public void updateRefreshToken(String newHash, int durationDays) {
        this.refreshTokenHash = newHash;
        this.refreshTokenExpiration = LocalDateTime.now().plusDays(durationDays);
    }

    /**
     * Gibt die Liste der Freizeiten zurück.
     * @return Liste der Freizeiten
     */
    public List<FreeTime> getFreeTimes() {
        return freeTimes;
    }

    /** Fügt ein Modul hinzu und stellt die bidirektionale Konsistenz sicher[cite: 789, 817].
     * @param module das hinzuzufügende Modul
     */
    public void addModule(Module module) {
        if (module != null) {
            this.modules.add(module);
        }
    }

    /** Entfernt ein Modul konsistent aus dem Profil[cite: 787, 817].
     * @param module das zu entfernende Modul
     */
    public void deleteModule(Module module) {
        if (module != null) {
            this.modules.remove(module);
        }
    }

    /** Fügt eine neue Zeitrestriktion (Freizeit) hinzu[cite: 788, 819].
     * @param freeTime die hinzuzufügende Freizeit
     */
    public void addFreeTime(FreeTime freeTime) {
        if (freeTime != null) {
            this.freeTimes.add(freeTime);
        }
    }

    /** Entfernt eine bestehende Zeitrestriktion[cite: 786, 819].
     * @param freeTime die zu entfernende Freizeit
     */
    public void deleteFreeTime(FreeTime freeTime) {
        if (freeTime != null) {
            this.freeTimes.remove(freeTime);
        }
    }


    /**
     * Gibt die Lernpräferenzen zurück.
     * @return die aktuellen Lernpräferenzen
     */
    public LearningPreferences getPreferences() {
        return preferences;
    }

    /**
     * Setzt die Lernpräferenzen.
     * @param preferences die neu zu setzenden Lernpräferenzen
     */
    public void setPreferences(LearningPreferences preferences) {
        this.preferences = preferences;
    }

    // GETTER & SETTER

    /**
     * Gibt die user id
     * @return die user id als UUID
     */
    public UUID getId() {
        return userId;
    }

    /**
     * Gibt den Benutzertyp zurück.
     * @return den Benutzertyp
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Setzt den Hash des Refresh-Tokens.
     * @param refreshTokenHash der neue Hash des Refresh-Tokens
     */
    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    /**
     * Gibt den Hash des Refresh-Tokens zurück.
     * @return den Hash des Refresh-Tokens
     */
    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    /**
     * Gibt den Benutzernamen zurück.
     * @return den Benutzernamen
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gibt die Liste der generierten wochenbasierten Lernpläne zurück.
     * @return Liste der generierten wochenbasierten Lernpläne
     */
    public List<LearningPlan> getLearningPlans() {
        return learningPlans;
    }

    /**
     * Gibt die Liste der dem Nutzer zugeordneten Studienmodule zurück.
     * @return Liste der dem Nutzer zugeordneten Studienmodule
     */
    public List<Module> getModules() {
        return modules;
    }

}
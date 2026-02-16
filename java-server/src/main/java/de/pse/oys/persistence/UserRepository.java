package de.pse.oys.persistence;

import java.util.Optional;
import java.util.UUID;

import de.pse.oys.domain.ExternalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.UserType;

/**
 * Repository für {@link User}-Entitäten.
 *
 * @author uqvfm
 * @version 1.1
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Findet einen Benutzer anhand von Username und Benutzertyp.
     *
     * @param username der gesuchte Username
     * @param type     der Benutzertyp
     * @return Optional mit dem gefundenen Benutzer, sonst leer
     */
    Optional<User> findByUsernameAndUserType(String username, UserType type);

    /**
     * Prüft, ob ein Benutzer mit dem angegebenen Username existiert.
     *
     * @param username der Username
     * @return {@code true}, wenn der Benutzer existiert, sonst {@code false}
     */
    boolean existsByUsername(String username);

    /**
     * Findet einen externen Benutzer anhand der externen Subject-ID und des Benutzertyps.
     *
     * @param externalSubjectId die externe Subject-ID des Identity Providers
     * @param userType          der Benutzertyp (passend zum Authentifizierungsanbieter)
     * @return Optional mit dem gefundenen externen Benutzer, sonst leer
     */
    Optional<ExternalUser> findByExternalSubjectIdAndUserType(String externalSubjectId, UserType userType);
}
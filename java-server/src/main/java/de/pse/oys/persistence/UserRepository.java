package de.pse.oys.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.UserType;

/**
 * UserRepository – Repository-Schnittstelle für User-Entitäten.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Findet einen Benutzer anhand des Benutzernamens und Typs.
     * @param username der Benutzername welcher gesucht wird
     * @param type der Benutzertyp welcher gesucht wird (lokaler oder externer Benutzer)
     * @return Optional des gefundenen Benutzers
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.userType = :type")
    Optional<User> findByNameAndType(@Param("username") String username,
                                     @Param("type") UserType type);

    /**
     * Prüft, ob ein Benutzer mit dem angegebenen Benutzernamen existiert.
     * @param username der Benutzername
     * @return true, wenn der Benutzer existiert, sonst false
     */
    boolean existsByUsername(String username);
}

package de.pse.oys.persistence;

import de.pse.oys.domain.Module;
import de.pse.oys.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für {@link Module}-Entitäten.
 *
 * @author uqvfm
 * @version 1.1
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    /**
     * Findet alle Module eines bestimmten Nutzers.
     *
     * @param userId die ID des Nutzers
     * @return Liste der Module des Nutzers
     */
    List<Module> findAllByUser_UserId(UUID userId);

    /**
     * Findet ein spezifisches Modul eines Nutzers anhand der Modul-ID und der Nutzer-ID.
     * Dies stellt sicher, dass ein Nutzer nur Zugriff auf seine eigenen Module hat.
     *
     * @param moduleId die ID des Moduls
     * @param userId   die ID des Nutzers
     * @return Optional mit dem Modul, falls die Kombination existiert
     */
    Optional<Module> findByModuleIdAndUser_UserId(UUID moduleId, UUID userId);

    UUID user(User user);
}
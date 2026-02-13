package de.pse.oys.persistence;

import de.pse.oys.domain.Module;
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
     * Findet ein Modul eines Nutzers anhand des Titels.
     *
     * @param userId      die ID des Nutzers
     * @param moduleTitle der Modultitel
     * @return Optional mit Modul, falls gefunden
     */
    Optional<Module> findByUser_UserIdAndTitle(UUID userId, String moduleTitle);
}
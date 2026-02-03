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
 * <p>
 * Umsetzung über Spring Data JPA Derived Queries:
 * Die Methodennamen werden zur Laufzeit analysiert und in passende SQL-Abfragen übersetzt
 * (Property-Traversal über {@code Module -> user -> userId}).
 * </p>
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    /**
     * Findet alle Module eines bestimmten Nutzers.
     *
     * @param userId die ID des Nutzers
     * @return Liste der Module des Nutzers (leer, wenn keine vorhanden sind)
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

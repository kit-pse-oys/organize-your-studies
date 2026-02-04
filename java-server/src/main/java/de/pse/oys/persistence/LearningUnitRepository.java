package de.pse.oys.persistence;

import de.pse.oys.domain.LearningUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository für {@link LearningUnit}-Entitäten.
 *
 * @author uqvfm
 * @version 1.0
 */
@Repository
public interface LearningUnitRepository extends JpaRepository<LearningUnit, UUID> {

    /**
     * Lädt alle Lerneinheiten, die zu Aufgaben gehören, deren Modul dem angegebenen User zugeordnet ist.
     *
     * @param userId ID des Users
     * @return Liste der Lerneinheiten des Users
     */
    List<LearningUnit> findAllByTask_Module_User_UserId(UUID userId);
}

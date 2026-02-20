package de.pse.oys.persistence;

import de.pse.oys.domain.FreeTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository für {@link FreeTime}-Entitäten.
 *
 * @author uqvfm
 * @version 1.1
 */
@Repository
public interface FreeTimeRepository extends JpaRepository<FreeTime, UUID> {

    /**
     * Lädt alle Freizeitblöcke, die dem angegebenen User zugeordnet sind.
     *
     * @param userId ID des Users
     * @return Liste der Freizeitblöcke des Users
     */
    List<FreeTime> findAllByUserId(UUID userId);
}
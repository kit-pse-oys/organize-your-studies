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
    List<FreeTime> findAllByUser_UserId(UUID userId);

    /**
     * Prüft, ob der angegebene User mindestens einen Freizeitblock besitzt.
     *
     * @param userId ID des Users
     * @return {@code true}, wenn mindestens ein Freizeitblock existiert, sonst {@code false}
     */
    boolean existsByUser_UserId(UUID userId);

    /**
     * Prüft, ob es für einen User bereits einen Freizeitblock gibt, der sich mit einem
     * angegebenen Zeitraum überschneidet – wobei ein bestimmter Block (z.B. beim Update)
     * ignoriert wird.
     *
     * @param userId    ID des Users
     * @param ignoreId  ID des Freizeitblocks, der bei der Prüfung ausgeschlossen wird
     * @param endTime   Ende des zu prüfenden Zeitraums
     * @param startTime Start des zu prüfenden Zeitraums
     * @return {@code true}, wenn eine Überschneidung existiert, sonst {@code false}
     */
    boolean existsByUser_UserIdAndFreeTimeIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID userId,
            UUID ignoreId,
            LocalTime endTime,
            LocalTime startTime
    );

}
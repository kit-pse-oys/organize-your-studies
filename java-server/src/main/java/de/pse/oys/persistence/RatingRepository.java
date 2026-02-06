package de.pse.oys.persistence;

import de.pse.oys.domain.UnitRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository für {@link UnitRating}-Entitäten.
 *
 * @author uqvfm
 * @version 1.1
 */
@Repository
public interface RatingRepository extends JpaRepository<UnitRating, UUID> {
}

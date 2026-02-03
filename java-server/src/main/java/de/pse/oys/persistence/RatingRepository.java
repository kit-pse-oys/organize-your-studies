package de.pse.oys.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.UnitRating;

/**
 * RatingRepository – Repository-Schnittstelle für UnitRating-Entitäten.
 */
@Repository
public interface RatingRepository extends JpaRepository<UnitRating, UUID> {
}

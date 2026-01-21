package de.pse.oys.persistence.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.rating.UnitRating;

@Repository
public interface RatingRepository extends JpaRepository<UnitRating, UUID> {

    @Query(value = "select * from ratings where unitid = :unitId", nativeQuery = true)
    Optional<UnitRating> findByUnitId(@Param("unitId") UUID unitId);
}

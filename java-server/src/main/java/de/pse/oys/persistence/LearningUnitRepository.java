package de.pse.oys.persistence;

import de.pse.oys.domain.LearningUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * LearningUnitRepository – Repository-Schnittstelle für Lerneinheiten.
 *
 * @author uhupo
 * @version 1.0
 */
@Repository
public interface LearningUnitRepository extends JpaRepository<LearningUnit, UUID> {
}

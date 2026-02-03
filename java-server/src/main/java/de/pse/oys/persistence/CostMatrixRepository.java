package de.pse.oys.persistence;

import de.pse.oys.domain.CostMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * CostMatrixRepository – Repository-Schnittstelle für CostMatrix-Entitäten.
 */
@Repository
public interface CostMatrixRepository extends JpaRepository<CostMatrix, UUID> {

    /**
     * Findet eine CostMatrix anhand der zugehörigen Task-ID.
     * @param taskId Die ID des Tasks.
     * @return Optional mit der gefundenen CostMatrix oder leer, wenn keine gefunden wurde.
     */
    Optional<CostMatrix> findByTask_TaskId(UUID taskId);
}

package de.pse.oys.persistence;

import java.util.Optional;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.pse.oys.domain.CostMatrix;

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
    @Query(value = "SELECT * FROM cost_matrices WHERE taskid = :tid", nativeQuery = true)
    Optional<CostMatrix> findByTaskId(@Param("tid") UUID taskId);
}

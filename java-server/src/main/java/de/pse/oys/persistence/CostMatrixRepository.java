package de.pse.oys.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.costs.CostMatrix;

@Repository
public interface CostMatrixRepository extends JpaRepository<CostMatrix, UUID> {

    @Query(value = "select * from cost_matrices where taskid = :tid", nativeQuery = true)
    Optional<CostMatrix> findByTaskId(@Param("tid") UUID taskId);
}

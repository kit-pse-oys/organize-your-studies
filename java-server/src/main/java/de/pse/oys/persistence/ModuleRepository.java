package de.pse.oys.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import de.pse.oys.domain.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    /**
     * Findet alle Module für einen bestimmten Benutzer.
     * @param userId die ID des Benutzers
     * @return Liste der Module des Benutzers
     */
    @Query(value = "SELECT * FROM modules WHERE userid = :uid", nativeQuery = true)
    List<Module> findByUserId(@Param("uid") UUID userId);
}

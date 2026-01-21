package de.pse.oys.persistence.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.module.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    @Query(value = "select * from modules where userid = :uid", nativeQuery = true)
    List<Module> findByUserId(@Param("uid") UUID userId);
}

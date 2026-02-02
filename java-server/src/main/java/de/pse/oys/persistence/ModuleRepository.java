package de.pse.oys.persistence;

import de.pse.oys.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    /**
     * Findet alle Module eines bestimmten Users.
     *
     * @param userId die ID des Users
     * @return Liste der Module des Users
     */
    @Query("SELECT m FROM Module m WHERE m.user.userId = :uid")
    List<Module> findByUserId(@Param("uid") UUID userId);

    /**
     * Findet ein Modul eines Users anhand des Titels.
     *
     * @param userId      die ID des Users
     * @param moduleTitle der Modultitel
     * @return Optional mit Modul, falls gefunden
     */
    @Query("SELECT m FROM Module m WHERE m.user.userId = :uid AND m.title = :title")
    Optional<Module> findByUserIdAndTitle(@Param("uid") UUID userId,
                                          @Param("title") String moduleTitle);
}

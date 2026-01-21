package de.pse.oys.persistence.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.task.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query(value = """
            select * from tasks
            where (fixed_deadline is not null and fixed_deadline < :d)
               or (time_frame_end is not null and time_frame_end < :d)
            """, nativeQuery = true)
    List<Task> findAllByDeadlineBefore(@Param("d") LocalDate deadline);

    @Query(value = "select * from tasks where moduleid = :mid", nativeQuery = true)
    List<Task> findByModuleId(@Param("mid") UUID moduleId);
}

package de.pse.oys.persistence.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.freetime.FreeTime;

@Repository
public interface FreeTimeRepository extends JpaRepository<FreeTime, UUID> {

    @Query(value = "select * from free_times where userid = :uid", nativeQuery = true)
    List<FreeTime> findByUserId(@Param("uid") UUID userId);

    @Query(value = """
            select * from free_times
            where userid = :uid
              and (
                    (specific_date is not null and specific_date between :start and :end)
                 or (weekday is not null)
              )
            """, nativeQuery = true)
    List<FreeTime> findFreeTimeInPeriod(@Param("uid") UUID userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);
}


package de.pse.oys.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// TODO: imports anpassen
import your.package.domain.user.User;
import your.package.domain.user.UserType;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where u.username = :username and u.userType = :type")
    Optional<User> findByNameAndType(@Param("username") String username,
                                     @Param("type") UserType type);

    boolean existsByUsername(String username);
}

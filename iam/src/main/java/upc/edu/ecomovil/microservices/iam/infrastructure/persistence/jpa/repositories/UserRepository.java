package upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;

import java.util.Optional;

/**
 * User repository interface.
 * <p>
 * This interface defines the contract for persisting User aggregates.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     * 
     * @param username the username
     * @return the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists by username.
     * 
     * @param username the username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);
}

package upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.Role;
import upc.edu.ecomovil.microservices.iam.domain.model.valueobjects.Roles;

import java.util.Optional;

/**
 * Role repository interface.
 * <p>
 * This interface defines the contract for persisting Role aggregates.
 * </p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by name.
     * 
     * @param name the role name
     * @return the role if found
     */
    Optional<Role> findByName(Roles name);

    /**
     * Check if a role exists by name.
     * 
     * @param name the role name
     * @return true if exists, false otherwise
     */
    boolean existsByName(Roles name);
}

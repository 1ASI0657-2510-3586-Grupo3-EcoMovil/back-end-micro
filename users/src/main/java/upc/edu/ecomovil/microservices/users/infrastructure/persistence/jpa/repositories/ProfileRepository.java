package upc.edu.ecomovil.microservices.users.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.EmailAddress;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.RucNumber;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // Aqui incluyo aquellos metodos que no estan en el JpaRepository
    Optional<Profile> findByEmail(EmailAddress emailAddress);

    Optional<Profile> findByRuc(RucNumber ruc);

    Optional<Profile> findByUserId(Long userId);

    List<Profile> findByPlanId(Long planId);
}

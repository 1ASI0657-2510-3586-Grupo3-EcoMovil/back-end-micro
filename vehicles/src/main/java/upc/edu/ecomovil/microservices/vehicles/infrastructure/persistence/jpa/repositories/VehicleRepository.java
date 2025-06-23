package upc.edu.ecomovil.microservices.vehicles.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findAllByOwnerId(Long ownerId);

    List<Vehicle> findAllByDetailsType(String type);

    List<Vehicle> findAllByIsAvailable(Boolean isAvailable);

    List<Vehicle> findAllByOwnerIdAndIsAvailable(Long ownerId, Boolean isAvailable);

    /**
     * Find vehicle by owner, name and year to prevent duplicates
     * Similar to how Users microservice prevents duplicate RUC numbers
     */
    @Query("SELECT v FROM Vehicle v WHERE v.ownerId = :ownerId AND v.details.name = :name AND v.details.year = :year")
    Optional<Vehicle> findByOwnerIdAndDetailsNameAndDetailsYear(
            @Param("ownerId") Long ownerId,
            @Param("name") String name,
            @Param("year") Integer year);
}

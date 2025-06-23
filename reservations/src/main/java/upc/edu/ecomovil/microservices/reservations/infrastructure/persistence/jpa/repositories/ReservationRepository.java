package upc.edu.ecomovil.microservices.reservations.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upc.edu.ecomovil.microservices.reservations.domain.model.aggregates.Reservation;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUserId(Long userId);

    List<Reservation> findAllByVehicleId(Long vehicleId);

    List<Reservation> findAllByStatus(String status);

    List<Reservation> findAllByUserIdAndStatus(Long userId, String status);

    List<Reservation> findAllByVehicleIdAndStatus(Long vehicleId, String status);
}

package upc.edu.ecomovil.microservices.reservations.application.internal.queryservices;

import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.reservations.domain.model.aggregates.Reservation;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsByUserIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsByVehicleIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetReservationByIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.services.ReservationQueryService;
import upc.edu.ecomovil.microservices.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public ReservationQueryServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Optional<Reservation> handle(GetReservationByIdQuery query) {
        return reservationRepository.findById(query.reservationId());
    }

    @Override
    public List<Reservation> handle(GetAllReservationsQuery query) {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByUserIdQuery query) {
        return reservationRepository.findAllByUserId(query.userId());
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByVehicleIdQuery query) {
        return reservationRepository.findAllByVehicleId(query.vehicleId());
    }
}

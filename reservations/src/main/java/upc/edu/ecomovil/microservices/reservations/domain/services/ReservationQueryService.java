package upc.edu.ecomovil.microservices.reservations.domain.services;

import upc.edu.ecomovil.microservices.reservations.domain.model.aggregates.Reservation;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsByUserIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsByVehicleIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetReservationByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ReservationQueryService {
    Optional<Reservation> handle(GetReservationByIdQuery query);

    List<Reservation> handle(GetAllReservationsQuery query);

    List<Reservation> handle(GetAllReservationsByUserIdQuery query);

    List<Reservation> handle(GetAllReservationsByVehicleIdQuery query);
}

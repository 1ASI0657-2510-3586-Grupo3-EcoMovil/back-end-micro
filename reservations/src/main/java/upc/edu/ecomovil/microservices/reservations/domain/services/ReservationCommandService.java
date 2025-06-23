package upc.edu.ecomovil.microservices.reservations.domain.services;

import upc.edu.ecomovil.microservices.reservations.domain.model.aggregates.Reservation;
import upc.edu.ecomovil.microservices.reservations.domain.model.commands.CreateReservationCommand;
import upc.edu.ecomovil.microservices.reservations.domain.model.commands.UpdateReservationStatusCommand;

import java.util.Optional;

public interface ReservationCommandService {
    Optional<Reservation> handle(CreateReservationCommand command);

    Optional<Reservation> handle(UpdateReservationStatusCommand command);
}

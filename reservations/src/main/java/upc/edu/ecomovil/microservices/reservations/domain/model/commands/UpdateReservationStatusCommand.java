package upc.edu.ecomovil.microservices.reservations.domain.model.commands;

public record UpdateReservationStatusCommand(
        Long reservationId,
        String status) {
}

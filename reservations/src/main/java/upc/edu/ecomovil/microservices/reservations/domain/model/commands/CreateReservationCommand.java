package upc.edu.ecomovil.microservices.reservations.domain.model.commands;

public record CreateReservationCommand(
        String status,
        Long vehicleId,
        Long userId,
        String startDate,
        String endDate,
        Double totalPrice,
        String reservationType,
        String notes) {
}

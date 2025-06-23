package upc.edu.ecomovil.microservices.reservations.interfaces.rest.resources;

public record CreateReservationResource(
        String status,
        Long vehicleId,
        String startDate,
        String endDate,
        Double totalPrice,
        String reservationType,
        String notes) {
}

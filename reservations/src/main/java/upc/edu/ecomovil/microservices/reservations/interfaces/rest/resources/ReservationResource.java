package upc.edu.ecomovil.microservices.reservations.interfaces.rest.resources;

public record ReservationResource(
        Long id,
        String status,
        Long vehicleId,
        Long userId,
        String startDate,
        String endDate,
        Double totalPrice,
        String reservationType,
        String notes,
        String createdAt,
        String updatedAt) {
}

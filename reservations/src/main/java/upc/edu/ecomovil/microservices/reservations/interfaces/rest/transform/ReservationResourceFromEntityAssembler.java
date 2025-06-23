package upc.edu.ecomovil.microservices.reservations.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.reservations.domain.model.aggregates.Reservation;
import upc.edu.ecomovil.microservices.reservations.interfaces.rest.resources.ReservationResource;

public class ReservationResourceFromEntityAssembler {
    public static ReservationResource toResourceFromEntity(Reservation entity) {
        return new ReservationResource(
                entity.getId(),
                entity.getStatus(),
                entity.getVehicleId(),
                entity.getUserId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getTotalPrice(),
                entity.getReservationType(),
                entity.getNotes(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null,
                entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
    }
}

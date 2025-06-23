package upc.edu.ecomovil.microservices.reservations.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.reservations.domain.model.commands.CreateReservationCommand;
import upc.edu.ecomovil.microservices.reservations.interfaces.rest.resources.CreateReservationResource;

public class CreateReservationCommandFromResourceAssembler {
    public static CreateReservationCommand toCommandFromResource(CreateReservationResource resource, Long userId) {
        return new CreateReservationCommand(
                resource.status(),
                resource.vehicleId(),
                userId,
                resource.startDate(),
                resource.endDate(),
                resource.totalPrice(),
                resource.reservationType(),
                resource.notes());
    }
}

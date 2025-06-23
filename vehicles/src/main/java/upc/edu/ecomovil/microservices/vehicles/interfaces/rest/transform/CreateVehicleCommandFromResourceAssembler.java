package upc.edu.ecomovil.microservices.vehicles.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.interfaces.rest.resources.CreateVehicleResource;

public class CreateVehicleCommandFromResourceAssembler {

    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource, Long ownerId) {
        return new CreateVehicleCommand(
                resource.type(),
                resource.name(),
                resource.year(),
                resource.review(),
                resource.priceRent(),
                resource.priceSell(),
                resource.isAvailable(),
                resource.imageUrl(),
                resource.lat(),
                resource.lng(),
                resource.description(),
                ownerId);
    }
}

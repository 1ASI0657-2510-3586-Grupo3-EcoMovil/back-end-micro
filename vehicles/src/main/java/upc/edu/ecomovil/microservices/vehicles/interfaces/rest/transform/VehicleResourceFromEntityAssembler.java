package upc.edu.ecomovil.microservices.vehicles.interfaces.rest.transform;

import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.interfaces.rest.resources.VehicleResource;

public class VehicleResourceFromEntityAssembler {

    public static VehicleResource toResourceFromEntity(Vehicle entity) {
        return new VehicleResource(
                entity.getId(),
                entity.getOwnerId(),
                entity.getType(),
                entity.getName(),
                entity.getYear(),
                entity.getReviewValue(),
                entity.getPriceRent(),
                entity.getPriceSell(),
                entity.getIsAvailable(),
                entity.getImageUrl(),
                entity.getLat(),
                entity.getLng(),
                entity.getDescription());
    }
}

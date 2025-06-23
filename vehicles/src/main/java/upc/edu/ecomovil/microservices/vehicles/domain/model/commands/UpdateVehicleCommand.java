package upc.edu.ecomovil.microservices.vehicles.domain.model.commands;

/**
 * Command to update vehicle details
 */
public record UpdateVehicleCommand(
        Long vehicleId,
        String type,
        String name,
        Integer year,
        Integer review,
        Double priceRent,
        Double priceSell,
        Boolean isAvailable,
        String imageUrl,
        Float lat,
        Float lng,
        String description) {
}

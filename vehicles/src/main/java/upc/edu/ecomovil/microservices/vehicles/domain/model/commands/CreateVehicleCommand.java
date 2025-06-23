package upc.edu.ecomovil.microservices.vehicles.domain.model.commands;

public record CreateVehicleCommand(
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
        String description,
        Long ownerId) {
}

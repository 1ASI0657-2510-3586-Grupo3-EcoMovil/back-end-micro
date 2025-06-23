package upc.edu.ecomovil.microservices.vehicles.interfaces.rest.resources;

public record CreateVehicleResource(
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

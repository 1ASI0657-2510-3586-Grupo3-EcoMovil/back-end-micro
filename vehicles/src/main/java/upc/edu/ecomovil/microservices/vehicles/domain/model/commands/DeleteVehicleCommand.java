package upc.edu.ecomovil.microservices.vehicles.domain.model.commands;

/**
 * Command to delete a vehicle
 */
public record DeleteVehicleCommand(Long vehicleId) {
}

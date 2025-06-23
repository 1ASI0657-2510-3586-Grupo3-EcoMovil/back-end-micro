package upc.edu.ecomovil.microservices.vehicles.domain.services;

import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.UpdateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.DeleteVehicleCommand;

import java.util.Optional;

/**
 * Vehicle command service interface
 * 
 * Defines the contract for handling vehicle-related commands
 * including creation, updates, and deletion of vehicles.
 */
public interface VehicleCommandService {

    /**
     * Handle create vehicle command
     * 
     * @param command the create vehicle command
     * @return the created vehicle
     */
    Optional<Vehicle> handle(CreateVehicleCommand command);

    /**
     * Handle update vehicle command
     * 
     * @param command the update vehicle command
     * @return the updated vehicle
     */
    Optional<Vehicle> handle(UpdateVehicleCommand command);

    /**
     * Handle delete vehicle command
     * 
     * @param command the delete vehicle command
     * @return true if deletion was successful, false otherwise
     */
    boolean handle(DeleteVehicleCommand command);
}

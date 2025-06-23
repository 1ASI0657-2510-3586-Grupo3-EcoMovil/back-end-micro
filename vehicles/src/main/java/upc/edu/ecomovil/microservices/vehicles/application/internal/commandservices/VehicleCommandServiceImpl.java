package upc.edu.ecomovil.microservices.vehicles.application.internal.commandservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.vehicles.application.internal.outboundservices.acl.ExternalUserService;
import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.UpdateVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.model.commands.DeleteVehicleCommand;
import upc.edu.ecomovil.microservices.vehicles.domain.services.VehicleCommandService;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.persistence.jpa.repositories.VehicleRepository;

import java.util.Optional;

@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleCommandServiceImpl.class);

    private final VehicleRepository vehicleRepository;
    private final ExternalUserService externalUserService;

    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository, ExternalUserService externalUserService) {
        this.vehicleRepository = vehicleRepository;
        this.externalUserService = externalUserService;
    }

    @Override
    public Optional<Vehicle> handle(CreateVehicleCommand command) {
        logger.info("Creating vehicle for ownerId: {}", command.ownerId());

        // Validate that the owner (user profile) exists - similar to how Users
        // validates Plans
        if (command.ownerId() != null) {
            logger.info("Validating owner profile with ID: {}", command.ownerId());
            var owner = externalUserService.fetchUserProfileById(command.ownerId());
            if (owner.isEmpty()) {
                logger.error("Owner validation failed - Profile with ID {} does not exist", command.ownerId());
                throw new IllegalArgumentException(
                        "El perfil de usuario con el ID " + command.ownerId() + " no existe");
            }
            logger.info("Owner profile validation successful for ID: {}", command.ownerId());
        } else {
            logger.warn("No ownerId provided - this should not happen as ownerId is extracted from JWT");
            throw new IllegalArgumentException("El ID del propietario es requerido para crear un vehículo");
        }

        // Check for duplicate vehicles (same owner, name, and year) - similar to Users
        // RUC validation
        vehicleRepository.findByOwnerIdAndDetailsNameAndDetailsYear(
                command.ownerId(), command.name(), command.year()).ifPresent(
                        existingVehicle -> {
                            logger.error(
                                    "Vehicle creation failed - Vehicle with name '{}' and year {} already exists for owner {}",
                                    command.name(), command.year(), command.ownerId());
                            throw new IllegalArgumentException(
                                    "Ya tienes un vehículo con el nombre '" + command.name() +
                                            "' del año " + command.year()
                                            + ". Cada vehículo debe tener un nombre único por año.");
                        });

        try {
            var vehicle = new Vehicle(command);
            var savedVehicle = vehicleRepository.save(vehicle);
            logger.info("Vehicle created successfully with ID: {}", savedVehicle.getId());
            return Optional.of(savedVehicle);
        } catch (Exception e) {
            logger.error("Error creating vehicle: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Vehicle> handle(UpdateVehicleCommand command) {
        logger.info("Updating vehicle with ID: {}", command.vehicleId());

        try {
            var vehicleOptional = vehicleRepository.findById(command.vehicleId());
            if (vehicleOptional.isEmpty()) {
                logger.warn("Vehicle with ID {} not found for update", command.vehicleId());
                return Optional.empty();
            }

            var vehicle = vehicleOptional.get();

            // Update vehicle properties
            if (command.type() != null || command.name() != null || command.year() != null) {
                vehicle.updateDetails(command.type(), command.name(), command.year());
            }

            if (command.review() != null) {
                vehicle.updateReview(command.review());
            }

            if (command.priceRent() != null || command.priceSell() != null) {
                vehicle.updatePrices(command.priceRent(), command.priceSell());
            }

            if (command.isAvailable() != null) {
                vehicle.updateAvailability(command.isAvailable());
            }

            if (command.imageUrl() != null) {
                vehicle.updateImageUrl(command.imageUrl());
            }

            if (command.lat() != null || command.lng() != null) {
                vehicle.updateLocation(command.lat(), command.lng());
            }

            if (command.description() != null) {
                vehicle.updateDescription(command.description());
            }

            var savedVehicle = vehicleRepository.save(vehicle);
            logger.info("Vehicle updated successfully with ID: {}", savedVehicle.getId());
            return Optional.of(savedVehicle);

        } catch (Exception e) {
            logger.error("Error updating vehicle {}: {}", command.vehicleId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean handle(DeleteVehicleCommand command) {
        logger.info("Deleting vehicle with ID: {}", command.vehicleId());

        try {
            var vehicleOptional = vehicleRepository.findById(command.vehicleId());
            if (vehicleOptional.isEmpty()) {
                logger.warn("Vehicle with ID {} not found for deletion", command.vehicleId());
                return false;
            }

            vehicleRepository.delete(vehicleOptional.get());
            logger.info("Vehicle with ID {} deleted successfully", command.vehicleId());
            return true;

        } catch (Exception e) {
            logger.error("Error deleting vehicle {}: {}", command.vehicleId(), e.getMessage(), e);
            return false;
        }
    }
}

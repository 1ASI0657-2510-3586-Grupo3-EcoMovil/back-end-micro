package upc.edu.ecomovil.microservices.vehicles.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetAllVehiclesQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetVehicleByIdQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetAllVehiclesByTypeQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetVehiclesByOwnerIdQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.services.VehicleCommandService;
import upc.edu.ecomovil.microservices.vehicles.domain.services.VehicleQueryService;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.persistence.jpa.repositories.VehicleRepository;
import upc.edu.ecomovil.microservices.vehicles.interfaces.rest.resources.CreateVehicleResource;
import upc.edu.ecomovil.microservices.vehicles.interfaces.rest.resources.VehicleResource;
import upc.edu.ecomovil.microservices.vehicles.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import upc.edu.ecomovil.microservices.vehicles.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.security.JwtUserDetails;
import upc.edu.ecomovil.microservices.vehicles.application.internal.outboundservices.acl.ExternalUserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Vehicle Management
 * 
 * Provides endpoints for creating, retrieving, updating, and deleting vehicles.
 * All endpoints require JWT authentication and appropriate role authorization.
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Vehicles", description = "Vehicle Management Endpoints")
public class VehicleController {

    private final VehicleQueryService vehicleQueryService;
    private final VehicleCommandService vehicleCommandService;
    private final VehicleRepository vehicleRepository;
    private final ExternalUserService externalUserService;

    public VehicleController(VehicleQueryService vehicleQueryService,
            VehicleCommandService vehicleCommandService,
            VehicleRepository vehicleRepository,
            ExternalUserService externalUserService) {
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleRepository = vehicleRepository;
        this.externalUserService = externalUserService;
    }

    /**
     * Create a new vehicle
     * Users can only create vehicles for themselves
     */
    @Operation(summary = "Create a new vehicle", description = "Creates a new vehicle for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<VehicleResource> createVehicle(@RequestBody CreateVehicleResource resource,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            log.info("Authenticated as: {}", username);

            // Extract userId from JWT token via JwtUserDetails
            Long userId = ((JwtUserDetails) userDetails).getUserId();

            log.info("Creating vehicle for userId: {} with type: {}", userId, resource.type());

            var createVehicleCommand = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource,
                    userId);
            var vehicle = vehicleCommandService.handle(createVehicleCommand);

            if (vehicle.isEmpty()) {
                log.warn("Vehicle creation failed - service returned empty");
                return ResponseEntity.badRequest().build();
            }

            var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle.get());
            log.info("Vehicle created successfully with ID: {}", vehicle.get().getId());
            return new ResponseEntity<>(vehicleResource, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            log.error("Vehicle creation failed due to validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during vehicle creation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all vehicles - public endpoint for browsing available vehicles
     */
    @Operation(summary = "Get all vehicles", description = "Gets all vehicles available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<VehicleResource>> getAllVehicles() {
        log.info("Getting all vehicles");

        var getAllVehiclesQuery = new GetAllVehiclesQuery();
        var vehicles = vehicleQueryService.handle(getAllVehiclesQuery);

        var vehicleResources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        log.info("Found {} vehicles", vehicleResources.size());
        return ResponseEntity.ok(vehicleResources);
    }

    /**
     * Get vehicles owned by the authenticated user
     */
    @Operation(summary = "Get my vehicles", description = "Gets all vehicles owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-vehicles")
    public ResponseEntity<List<VehicleResource>> getMyVehicles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Extract userId from JWT token via JwtUserDetails
        Long userId = ((JwtUserDetails) authentication.getPrincipal()).getUserId();

        log.info("Getting vehicles for user: {} (ID: {})", username, userId);

        var getVehiclesByOwnerIdQuery = new GetVehiclesByOwnerIdQuery(userId);
        var vehicles = vehicleQueryService.handle(getVehiclesByOwnerIdQuery);

        var vehicleResources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(vehicleResources);
    }

    /**
     * Get a specific vehicle by ID
     * Users can only access their own vehicles, admins can access any vehicle
     */
    @Operation(summary = "Get a vehicle by ID", description = "Gets a vehicle by the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResource> getVehicle(@PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {

        var getVehicleByIdQuery = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(getVehicleByIdQuery);

        if (vehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if user owns the vehicle or is admin
        Long userId = ((JwtUserDetails) userDetails).getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !vehicle.get().getOwnerId().equals(userId)) {
            log.warn("User {} attempted to access vehicle {} owned by {}", userId, vehicleId,
                    vehicle.get().getOwnerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle.get());
        return ResponseEntity.ok(vehicleResource);
    }

    /**
     * Get vehicles by type
     */
    @Operation(summary = "Get vehicles by type", description = "Gets all vehicles of a specific type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle type")
    })
    @GetMapping("/type/{vehicleType}")
    public ResponseEntity<List<VehicleResource>> getVehiclesByType(@PathVariable String vehicleType) {
        try {
            var getVehiclesByTypeQuery = new GetAllVehiclesByTypeQuery(vehicleType);
            var vehicles = vehicleQueryService.handle(getVehiclesByTypeQuery);

            var vehicleResources = vehicles.stream()
                    .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(vehicleResources);
        } catch (IllegalArgumentException e) {
            log.error("Invalid vehicle type: {}", vehicleType);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update a vehicle
     * Users can only update their own vehicles
     */
    @Operation(summary = "Update a vehicle", description = "Updates a vehicle owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleResource> updateVehicle(@PathVariable Long vehicleId,
            @RequestBody CreateVehicleResource resource,
            @AuthenticationPrincipal UserDetails userDetails) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((JwtUserDetails) authentication.getPrincipal()).getUserId();

        // Find the vehicle
        var vehicleOptional = vehicleRepository.findById(vehicleId);
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vehicle vehicle = vehicleOptional.get();

        // Check if user owns the vehicle or is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !vehicle.getOwnerId().equals(userId)) {
            log.warn("User {} attempted to update vehicle {} owned by {}", userId, vehicleId, vehicle.getOwnerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Update vehicle properties
            vehicle.updateDetails(resource.type(), resource.name(), resource.year());
            vehicle.updatePrices(resource.priceRent(), resource.priceSell());
            vehicle.updateAvailability(resource.isAvailable());
            vehicle.updateImageUrl(resource.imageUrl());
            vehicle.updateLocation(resource.lat(), resource.lng());
            vehicle.updateDescription(resource.description());

            var savedVehicle = vehicleRepository.save(vehicle);
            var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(savedVehicle);

            return ResponseEntity.ok(vehicleResource);
        } catch (Exception e) {
            log.error("Error updating vehicle {}: {}", vehicleId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a vehicle
     * Users can only delete their own vehicles
     */
    @Operation(summary = "Delete a vehicle", description = "Deletes a vehicle owned by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = ((JwtUserDetails) userDetails).getUserId();

        // Find the vehicle
        var vehicleOptional = vehicleRepository.findById(vehicleId);
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vehicle vehicle = vehicleOptional.get();

        // Check if user owns the vehicle or is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !vehicle.getOwnerId().equals(userId)) {
            log.warn("User {} attempted to delete vehicle {} owned by {}", userId, vehicleId, vehicle.getOwnerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            vehicleRepository.delete(vehicle);
            log.info("Vehicle {} deleted successfully by user {}", vehicleId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting vehicle {}: {}", vehicleId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get vehicles by owner ID (Admin only)
     */
    @Operation(summary = "Get vehicles by owner ID", description = "Gets all vehicles owned by a specific user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/admin/owner/{ownerId}")
    public ResponseEntity<List<VehicleResource>> getVehiclesByOwnerId(@PathVariable Long ownerId) {
        var getVehiclesByOwnerIdQuery = new GetVehiclesByOwnerIdQuery(ownerId);
        var vehicles = vehicleQueryService.handle(getVehiclesByOwnerIdQuery);

        var vehicleResources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(vehicleResources);
    }

    /**
     * Test endpoint to verify JWT integration and user validation
     */
    @Operation(summary = "Test JWT integration", description = "Verifies that JWT authentication works and user profile can be retrieved")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Integration working correctly"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    @GetMapping("/test-integration")
    public ResponseEntity<String> testIntegration(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Long userId = ((JwtUserDetails) userDetails).getUserId();

            log.info("JWT Integration Test - Username: {}, UserId: {}", username, userId);

            return ResponseEntity.ok(String.format(
                    "✅ JWT Integration Working!\n" +
                            "Username: %s\n" +
                            "User ID: %d\n" +
                            "Authorities: %s\n" +
                            "Vehicles Service is ready to create vehicles!",
                    username, userId, userDetails.getAuthorities()));

        } catch (Exception e) {
            log.error("JWT Integration Test failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ JWT Integration Failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint to verify communication with Users service
     */
    @Operation(summary = "Test Users service integration", description = "Verifies communication with Users microservice")
    @GetMapping("/test-users-integration")
    public ResponseEntity<String> testUsersIntegration(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = ((JwtUserDetails) userDetails).getUserId();
            log.info("Testing Users service integration for userId: {}", userId);

            var userProfile = externalUserService.fetchUserProfileById(userId);

            if (userProfile.isPresent()) {
                var profile = userProfile.get();
                return ResponseEntity.ok(String.format(
                        "✅ Users Service Integration Working!\n" +
                                "Profile ID: %d\n" +
                                "Name: %s %s\n" +
                                "Email: %s\n" +
                                "Plan ID: %d\n" +
                                "Communication with Users service is successful!",
                        profile.getId(), profile.getFirstName(), profile.getLastName(),
                        profile.getEmail(), profile.getPlanId()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("❌ User profile not found in Users service for ID: " + userId);
            }

        } catch (Exception e) {
            log.error("Users service integration test failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ Users Service Integration Failed: " + e.getMessage());
        }
    }

    /**
     * Get a specific vehicle by ID - public endpoint for reservations
     * Any authenticated user can access this to view vehicle details for
     * reservations
     */
    @Operation(summary = "Get vehicle for reservation", description = "Gets vehicle information for reservation purposes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/public/{vehicleId}")
    public ResponseEntity<VehicleResource> getVehicleForReservation(@PathVariable Long vehicleId) {
        var getVehicleByIdQuery = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(getVehicleByIdQuery);

        if (vehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle.get());
        return ResponseEntity.ok(vehicleResource);
    }
}

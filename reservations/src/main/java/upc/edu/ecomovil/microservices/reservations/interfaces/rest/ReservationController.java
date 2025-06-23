package upc.edu.ecomovil.microservices.reservations.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import upc.edu.ecomovil.microservices.reservations.application.internal.outboundservices.acl.ExternalUserService;
import upc.edu.ecomovil.microservices.reservations.application.internal.outboundservices.acl.ExternalVehicleService;
import upc.edu.ecomovil.microservices.reservations.domain.model.commands.UpdateReservationStatusCommand;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsByUserIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsByVehicleIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetAllReservationsQuery;
import upc.edu.ecomovil.microservices.reservations.domain.model.queries.GetReservationByIdQuery;
import upc.edu.ecomovil.microservices.reservations.domain.services.ReservationCommandService;
import upc.edu.ecomovil.microservices.reservations.domain.services.ReservationQueryService;
import upc.edu.ecomovil.microservices.reservations.interfaces.rest.resources.CreateReservationResource;
import upc.edu.ecomovil.microservices.reservations.interfaces.rest.resources.ReservationResource;
import upc.edu.ecomovil.microservices.reservations.interfaces.rest.transform.CreateReservationCommandFromResourceAssembler;
import upc.edu.ecomovil.microservices.reservations.interfaces.rest.transform.ReservationResourceFromEntityAssembler;
import upc.edu.ecomovil.microservices.reservations.infrastructure.security.JwtUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Reservations", description = "Reservation Management Endpoints")
public class ReservationController {
    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ExternalUserService externalUserService;
    private final ExternalVehicleService externalVehicleService;

    public ReservationController(ReservationQueryService reservationQueryService,
            ReservationCommandService reservationCommandService,
            ExternalUserService externalUserService,
            ExternalVehicleService externalVehicleService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
        this.externalUserService = externalUserService;
        this.externalVehicleService = externalVehicleService;
    }

    @Operation(summary = "Create a Reservation", description = "Creates a Reservation with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User or Vehicle not found")
    })
    @PostMapping
    public ResponseEntity<ReservationResource> createReservation(@RequestBody CreateReservationResource resource,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            Long authenticatedUserId = ((JwtUserDetails) userDetails).getUserId();

            log.info("User {} (ID: {}) creating reservation for vehicle {}", username, authenticatedUserId,
                    resource.vehicleId());

            // Validate user exists
            var userProfile = externalUserService.fetchUserProfileById(authenticatedUserId);
            if (userProfile.isEmpty()) {
                log.warn("User profile not found for ID: {}", authenticatedUserId);
                return ResponseEntity.notFound().build();
            }

            // Validate vehicle exists
            var vehicle = externalVehicleService.fetchVehicleById(resource.vehicleId());
            if (vehicle.isEmpty()) {
                log.warn("Vehicle not found for ID: {}", resource.vehicleId());
                return ResponseEntity.notFound().build();
            }

            var createReservationCommand = CreateReservationCommandFromResourceAssembler.toCommandFromResource(resource,
                    authenticatedUserId);
            var reservation = reservationCommandService.handle(createReservationCommand);
            if (reservation.isEmpty()) {
                log.warn("Reservation creation failed for user {} and vehicle {}", authenticatedUserId,
                        resource.vehicleId());
                return ResponseEntity.badRequest().build();
            }

            var reservationResource = ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation.get());
            log.info("Reservation created successfully with ID: {}", reservation.get().getId());
            return new ResponseEntity<>(reservationResource, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating reservation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all Reservations", description = "Gets all Reservations in the system (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/all")
    public ResponseEntity<List<ReservationResource>> getAllReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Check if user is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            log.warn("Non-admin user {} attempted to access all reservations", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var getAllReservationsQuery = new GetAllReservationsQuery();
        var reservations = reservationQueryService.handle(getAllReservationsQuery);
        var reservationResources = reservations.stream()
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservationResources);
    }

    /**
     * Get reservations owned by the authenticated user
     */
    @Operation(summary = "Get my reservations", description = "Gets all reservations made by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationResource>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((JwtUserDetails) userDetails).getUserId();
        String username = userDetails.getUsername();

        log.info("Getting reservations for user: {} (ID: {})", username, userId);

        var getAllReservationsByUserIdQuery = new GetAllReservationsByUserIdQuery(userId);
        var reservations = reservationQueryService.handle(getAllReservationsByUserIdQuery);

        var reservationResources = reservations.stream()
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        log.info("Found {} reservations for user {}", reservationResources.size(), username);
        return ResponseEntity.ok(reservationResources);
    }

    @Operation(summary = "Get Reservation by ID", description = "Gets a specific Reservation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResource> getReservationById(@PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        var getReservationByIdQuery = new GetReservationByIdQuery(reservationId);
        var reservation = reservationQueryService.handle(getReservationByIdQuery);

        if (reservation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if user owns the reservation or is admin
        Long userId = ((JwtUserDetails) userDetails).getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !reservation.get().getUserId().equals(userId)) {
            log.warn("User {} attempted to access reservation {} owned by {}", userId, reservationId,
                    reservation.get().getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var reservationResource = ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation.get());
        return ResponseEntity.ok(reservationResource);
    }

    @Operation(summary = "Get Reservations by User ID", description = "Gets all Reservations for a specific user (Admin only or own reservations)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "No reservations found for user")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResource>> getAllReservationsByUserId(@PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long authenticatedUserId = ((JwtUserDetails) userDetails).getUserId();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            // Users can only access their own reservations, admins can access any
            if (!isAdmin && !authenticatedUserId.equals(userId)) {
                log.warn("User {} attempted to access reservations for user {}", authenticatedUserId, userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validate user exists
            var userProfile = externalUserService.fetchUserProfileById(userId);
            if (userProfile.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var getAllReservationsByUserIdQuery = new GetAllReservationsByUserIdQuery(userId);
            var reservations = reservationQueryService.handle(getAllReservationsByUserIdQuery);

            var reservationResources = reservations.stream()
                    .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservationResources);
        } catch (Exception e) {
            log.error("Error getting reservations for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get Reservations by Vehicle ID", description = "Gets all Reservations for a specific vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations found"),
            @ApiResponse(responseCode = "404", description = "No reservations found for vehicle")
    })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ReservationResource>> getAllReservationsByVehicleId(@PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long authenticatedUserId = ((JwtUserDetails) userDetails).getUserId();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            // Validate vehicle exists
            var vehicle = externalVehicleService.fetchVehicleById(vehicleId);
            if (vehicle.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns the vehicle or is admin
            if (!isAdmin && !vehicle.get().getOwnerId().equals(authenticatedUserId)) {
                log.warn("User {} attempted to access reservations for vehicle {} owned by {}",
                        authenticatedUserId, vehicleId, vehicle.get().getOwnerId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            var getAllReservationsByVehicleIdQuery = new GetAllReservationsByVehicleIdQuery(vehicleId);
            var reservations = reservationQueryService.handle(getAllReservationsByVehicleIdQuery);

            var reservationResources = reservations.stream()
                    .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservationResources);
        } catch (Exception e) {
            log.error("Error getting reservations for vehicle {}: {}", vehicleId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update the status of a Reservation", description = "Updates the status of a Reservation with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation updated"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PutMapping("/{reservationId}/status")
    public ResponseEntity<ReservationResource> updateReservationStatus(@PathVariable Long reservationId,
            @RequestBody String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Check if reservation exists and user has permission
            var getReservationByIdQuery = new GetReservationByIdQuery(reservationId);
            var existingReservation = reservationQueryService.handle(getReservationByIdQuery);

            if (existingReservation.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Long userId = ((JwtUserDetails) userDetails).getUserId();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            // Users can only update their own reservations, admins can update any
            if (!isAdmin && !existingReservation.get().getUserId().equals(userId)) {
                log.warn("User {} attempted to update reservation {} owned by {}", userId, reservationId,
                        existingReservation.get().getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            var updateCommand = new UpdateReservationStatusCommand(reservationId, status);
            var reservation = reservationCommandService.handle(updateCommand);
            if (reservation.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var reservationResource = ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation.get());
            return ResponseEntity.ok(reservationResource);
        } catch (Exception e) {
            log.error("Error updating reservation status for ID {}: {}", reservationId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete a Reservation", description = "Marks a Reservation as deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Check if reservation exists and user has permission
            var getReservationByIdQuery = new GetReservationByIdQuery(reservationId);
            var existingReservation = reservationQueryService.handle(getReservationByIdQuery);

            if (existingReservation.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Long userId = ((JwtUserDetails) userDetails).getUserId();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            // Users can only delete their own reservations, admins can delete any
            if (!isAdmin && !existingReservation.get().getUserId().equals(userId)) {
                log.warn("User {} attempted to delete reservation {} owned by {}", userId, reservationId,
                        existingReservation.get().getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            var deleteCommand = new UpdateReservationStatusCommand(reservationId, "DELETED");
            var reservation = reservationCommandService.handle(deleteCommand);
            if (reservation.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            log.info("Reservation {} deleted successfully by user {}", reservationId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting reservation {}: {}", reservationId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}

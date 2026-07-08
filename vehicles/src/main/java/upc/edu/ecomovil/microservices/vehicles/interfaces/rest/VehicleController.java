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
import org.springframework.web.multipart.MultipartFile;
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

import upc.edu.ecomovil.microservices.vehicles.infrastructure.aws.S3Service;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.aws.BedrockChatService;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.aws.IoTCoreService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final S3Service s3Service;
    private final BedrockChatService bedrockChatService;
    private final IoTCoreService iotCoreService;

    public VehicleController(VehicleQueryService vehicleQueryService,
            VehicleCommandService vehicleCommandService,
            VehicleRepository vehicleRepository,
            ExternalUserService externalUserService,
            S3Service s3Service,
            BedrockChatService bedrockChatService,
            IoTCoreService iotCoreService) {
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleRepository = vehicleRepository;
        this.externalUserService = externalUserService;
        this.s3Service = s3Service;
        this.bedrockChatService = bedrockChatService;
        this.iotCoreService = iotCoreService;
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
     * Upload a vehicle image to S3
     */
    @Operation(summary = "Upload vehicle image", description = "Uploads an image file to S3 and returns its URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            String url = s3Service.uploadFile(file);
            log.info("User {} uploaded vehicle image: {}", userDetails.getUsername(), url);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed"));
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

    public record ChatHistoryTurn(String role, String text) {
    }

    public record ChatRequest(String message, Float lat, Float lng, String userName, List<ChatHistoryTurn> history) {
    }

    public record ChatSuggestion(Long id, String name, String type, Double priceSell, Double priceRent,
            String imageUrl, Double distanceKm) {
    }

    public record ChatResponse(String reply, List<ChatSuggestion> suggestions) {
    }

    private static final java.util.regex.Pattern BUDGET_PATTERN = java.util.regex.Pattern.compile(
            "s/\\.?\\s*(\\d+(?:\\.\\d+)?)|(\\d+(?:\\.\\d+)?)\\s*soles?", java.util.regex.Pattern.CASE_INSENSITIVE);

    // ponytail: LLMs are unreliable at numeric filtering over a list in plain
    // text, so the budget constraint is applied here, deterministically, the
    // same way distance already is - the model only ever sees pre-filtered
    // candidates, never the full catalog.
    private static Double extractBudget(String text) {
        var matcher = BUDGET_PATTERN.matcher(text);
        Double last = null;
        while (matcher.find()) {
            String g = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            try {
                last = Double.parseDouble(g);
            } catch (NumberFormatException ignored) {
            }
        }
        return last;
    }

    /**
     * Sales chatbot - public endpoint, no auth required so visitors browsing
     * without an account can still get suggestions.
     */
    @Operation(summary = "Chat with the sales bot", description = "Suggests nearby available vehicles via Bedrock")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        var available = vehicleRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsAvailable()))
                .toList();

        String pastMessages = request.history() == null ? ""
                : request.history().stream().map(ChatHistoryTurn::text).reduce("", (a, b) -> a + " " + b);
        Double budget = extractBudget(pastMessages + " " + request.message());

        var withinBudget = budget == null ? available
                : available.stream().filter(v -> v.getPriceSell() != null && v.getPriceSell() <= budget).toList();
        var pool = withinBudget.isEmpty() ? available : withinBudget;

        boolean hasLocation = request.lat() != null && request.lng() != null;
        var ranked = pool.stream()
                .sorted(budget != null || !hasLocation
                        ? Comparator.comparing(v -> v.getPriceSell() == null ? Double.MAX_VALUE : v.getPriceSell())
                        : Comparator.comparingDouble(v -> distanceKm(request.lat(), request.lng(), v.getLat(), v.getLng())))
                .limit(3)
                .toList();

        // Greeting-only → pass empty catalog so the model text also stays clean (no vehicle names).
        // Alternative request → skip candidate #1 so the model sees a different pool.
        boolean greetingOnly = isGreetingOnly(request.message());
        boolean wantsAlternative = !greetingOnly && isAlternativeRequest(request.message());
        List<Vehicle> candidates;
        if (greetingOnly) {
            candidates = List.of();
        } else if (wantsAlternative && ranked.size() > 1) {
            candidates = ranked.subList(1, ranked.size());
        } else {
            candidates = ranked;
        }

        var history = request.history() == null ? null
                : request.history().stream()
                        .map(t -> new BedrockChatService.ChatTurn(t.role(), t.text()))
                        .toList();

        String rawReply = bedrockChatService.chat(request.message(), candidates, request.userName(), history);

        // Extract vehicle IDs from hidden [vid:N] tags, then strip them from the
        // user-visible reply so the user never sees "[vid:3]".
        java.util.regex.Pattern vidPattern = java.util.regex.Pattern.compile("\\[vid:(\\d+)\\]");
        java.util.regex.Matcher vidMatcher = vidPattern.matcher(rawReply);
        java.util.Set<Long> mentionedIds = new java.util.HashSet<>();
        while (vidMatcher.find()) mentionedIds.add(Long.parseLong(vidMatcher.group(1)));
        String reply = rawReply.replaceAll("\\[vid:\\d+\\]", "").trim();

        List<ChatSuggestion> suggestions;
        suggestions = candidates.stream()
                .filter(v -> mentionedIds.contains(v.getId()))
                .map(v -> new ChatSuggestion(v.getId(), v.getName(), v.getType(), v.getPriceSell(), v.getPriceRent(),
                        v.getImageUrl(), hasLocation ? distanceKm(request.lat(), request.lng(), v.getLat(), v.getLng()) : null))
                .toList();

        return ResponseEntity.ok(new ChatResponse(reply, suggestions));
    }

    private static final java.util.regex.Pattern GREETING_PATTERN = java.util.regex.Pattern.compile(
            "^\\s*(hola|hi|hey|buenas|buenos\\s+d[ií]as|buenas\\s+tardes|buenas\\s+noches|saludos|ola|ey)[!?.\\s]*$",
            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);

    private static boolean isGreetingOnly(String message) {
        return message != null && GREETING_PATTERN.matcher(message.trim()).matches();
    }

    private static final java.util.regex.Pattern ALTERNATIVE_PATTERN = java.util.regex.Pattern.compile(
            ".*\\b(otra|otro|diferente|alternativa|cambio|cambia|siguiente|distinto|no ese|no me gusta|algo mas|algo más)\\b.*",
            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);

    private static boolean isAlternativeRequest(String message) {
        return message != null && ALTERNATIVE_PATTERN.matcher(message.trim()).matches();
    }

    // ponytail: small in-memory dataset, plain Haversine beats adding a
    // geospatial query/library for a handful of vehicles.
    private static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // -----------------------------------------------------------------------
    // IoT endpoints
    // -----------------------------------------------------------------------

    /**
     * Receives telemetry pushed by the Lambda bridge (IoT Rule → Lambda → here).
     * Not JWT-protected — authenticated via a shared secret header X-IoT-Key.
     * POST /api/v1/vehicles/{vehicleId}/iot-telemetry
     */
    public record IoTTelemetryRequest(
            String deviceId,
            Float lat,
            Float lng,
            Boolean fallDetected,
            Boolean isLocked,
            Float speedKmh,
            Boolean panicActive) {}

    public record GeofenceRequest(Float centerLat, Float centerLng, Integer radiusM) {}

    @Operation(summary = "Receive IoT telemetry", description = "Internal endpoint called by Lambda bridge from AWS IoT Core")
    @PutMapping("/{vehicleId}/iot-telemetry")
    public ResponseEntity<Void> receiveIoTTelemetry(
            @PathVariable Long vehicleId,
            @RequestBody IoTTelemetryRequest body,
            @RequestHeader(value = "X-IoT-Key", required = false) String iotKey) {

        String expectedKey = System.getenv("IOT_KEY");
        if (expectedKey != null && !expectedKey.equals(iotKey)) {
            log.warn("IoT telemetry rejected: invalid X-IoT-Key for vehicle {}", vehicleId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Vehicle vehicle = vehicleOpt.get();
        boolean wasBreached = Boolean.TRUE.equals(vehicle.getGeofenceBreached());

        vehicle.updateIoTTelemetry(
                body.deviceId(),
                body.lat(),
                body.lng(),
                Boolean.TRUE.equals(body.fallDetected()),
                Boolean.TRUE.equals(body.isLocked()),
                body.speedKmh(),
                body.panicActive());
        vehicleRepository.save(vehicle);

        boolean nowBreached = Boolean.TRUE.equals(vehicle.getGeofenceBreached());
        if (!wasBreached && nowBreached && vehicle.getIotDeviceId() != null) {
            iotCoreService.sendCommand(vehicle.getIotDeviceId(), "LOCK");
            log.warn("Geofence breached for vehicle {} — LOCK sent to device {}",
                    vehicleId, vehicle.getIotDeviceId());
        }

        log.info("IoT telemetry updated for vehicle {}: lat={} lng={} locked={} fall={} speed={} panic={} geofenceBreached={}",
                vehicleId, body.lat(), body.lng(), body.isLocked(), body.fallDetected(),
                body.speedKmh(), body.panicActive(), nowBreached);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Set geofence for vehicle", description = "Saves the owner-defined geofence center and radius")
    @PutMapping("/{vehicleId}/geofence")
    public ResponseEntity<VehicleResource> setGeofence(
            @PathVariable Long vehicleId,
            @RequestBody GeofenceRequest body,
            @AuthenticationPrincipal UserDetails userDetails) {

        var vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) return ResponseEntity.notFound().build();

        Vehicle vehicle = vehicleOpt.get();
        Long userId = ((JwtUserDetails) userDetails).getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !vehicle.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        vehicle.setGeofence(body.centerLat(), body.centerLng(), body.radiusM());
        var saved = vehicleRepository.save(vehicle);
        log.info("Geofence set for vehicle {}: center=({},{}) radius={}m",
                vehicleId, body.centerLat(), body.centerLng(), body.radiusM());
        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(saved));
    }

    /**
     * Sends a LOCK command to the ESP32 via AWS IoT Core MQTT.
     * POST /api/v1/vehicles/{vehicleId}/lock
     */
    @Operation(summary = "Lock vehicle", description = "Sends LOCK command to the IoT device attached to this vehicle")
    @PostMapping("/{vehicleId}/lock")
    public ResponseEntity<VehicleResource> lockVehicle(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {

        var vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) return ResponseEntity.notFound().build();

        Vehicle vehicle = vehicleOpt.get();
        Long userId = ((JwtUserDetails) userDetails).getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !vehicle.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (vehicle.getIotDeviceId() == null) {
            return ResponseEntity.badRequest().build();
        }

        iotCoreService.sendCommand(vehicle.getIotDeviceId(), "LOCK");
        vehicle.setLocked(true);
        var saved = vehicleRepository.save(vehicle);
        log.info("LOCK sent to device {} (vehicle {})", vehicle.getIotDeviceId(), vehicleId);
        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(saved));
    }

    /**
     * Sends an UNLOCK command to the ESP32 via AWS IoT Core MQTT.
     * POST /api/v1/vehicles/{vehicleId}/unlock
     */
    @Operation(summary = "Unlock vehicle", description = "Sends UNLOCK command to the IoT device attached to this vehicle")
    @PostMapping("/{vehicleId}/unlock")
    public ResponseEntity<VehicleResource> unlockVehicle(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {

        var vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) return ResponseEntity.notFound().build();

        Vehicle vehicle = vehicleOpt.get();
        Long userId = ((JwtUserDetails) userDetails).getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !vehicle.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (vehicle.getIotDeviceId() == null) {
            return ResponseEntity.badRequest().build();
        }

        iotCoreService.sendCommand(vehicle.getIotDeviceId(), "UNLOCK");
        vehicle.setLocked(false); // also resets fallDetected + panicActive + geofenceBreached
        var saved = vehicleRepository.save(vehicle);
        log.info("UNLOCK sent to device {} (vehicle {})", vehicle.getIotDeviceId(), vehicleId);
        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(saved));
    }
}

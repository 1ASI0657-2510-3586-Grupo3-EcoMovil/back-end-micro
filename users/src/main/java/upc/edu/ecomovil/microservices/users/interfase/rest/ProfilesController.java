package upc.edu.ecomovil.microservices.users.interfase.rest;

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
import upc.edu.ecomovil.microservices.users.infrastructure.security.JwtUserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetAllProfilesQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByIdQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfilesByPlanIdQuery;
import upc.edu.ecomovil.microservices.users.domain.services.ProfileCommandService;
import upc.edu.ecomovil.microservices.users.domain.services.ProfileQueryService;
import upc.edu.ecomovil.microservices.users.infrastructure.persistence.jpa.repositories.ProfileRepository;
import upc.edu.ecomovil.microservices.users.interfase.rest.resources.CreateProfileResource;
import upc.edu.ecomovil.microservices.users.interfase.rest.resources.ProfileResource;
import upc.edu.ecomovil.microservices.users.interfase.rest.transform.CreateProfileCommandFromResourceAssembler;
import upc.edu.ecomovil.microservices.users.interfase.rest.transform.ProfileResourceFromEntityAssembler;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile Management Endpoints")
public class ProfilesController {
    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;
    private final ProfileRepository profileRepository;

    public ProfilesController(ProfileQueryService profileQueryService, ProfileCommandService profileCommandService,
            ProfileRepository profileRepository) {
        this.profileQueryService = profileQueryService;
        this.profileCommandService = profileCommandService;
        this.profileRepository = profileRepository;
    }

    @PostMapping
    public ResponseEntity<ProfileResource> createProfile(@RequestBody CreateProfileResource resource,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            log.info("Authenticated as: {}", username);

            // Extract userId from JWT token via JwtUserDetails
            Long userId = ((JwtUserDetails) userDetails).getUserId();

            log.info("Creating profile for userId: {} with planId: {}", userId, resource.planId());

            var createProfileCommand = CreateProfileCommandFromResourceAssembler.toCommandFromResource(resource, userId);
            var profile = profileCommandService.handle(createProfileCommand);

            if (profile.isEmpty()) {
                log.warn("Profile creation failed - service returned empty");
                return ResponseEntity.badRequest().build();
            }

            var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
            log.info("Profile created successfully with ID: {}", profile.get().getId());
            return new ResponseEntity<>(profileResource, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            log.error("Profile creation failed due to validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during profile creation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get a profile by ID", description = "Gets a profile by the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })

    @GetMapping("/me")
    public ResponseEntity<ProfileResource> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Extract userId from JWT token via JwtUserDetails
        Long userId = ((JwtUserDetails) authentication.getPrincipal()).getUserId();

        var profileOptional = profileRepository.findByUserId(userId);
        if (profileOptional.isEmpty())
            return ResponseEntity.notFound().build();

        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profileOptional.get());
        return ResponseEntity.ok(profileResource);
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResource> getProfile(@PathVariable Long profileId) {
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty())
            return ResponseEntity.notFound().build();

        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);
    }

    @GetMapping
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var getAllProfilesQuery = new GetAllProfilesQuery();
        var profiles = profileQueryService.handle(getAllProfilesQuery);
        var profileResources = profiles.stream().map(ProfileResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profileResources);
    }

    @PutMapping
    public ResponseEntity<ProfileResource> updateProfile(@RequestBody CreateProfileResource resource) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Extract userId from JWT token via JwtUserDetails
        Long userId = ((JwtUserDetails) authentication.getPrincipal()).getUserId();

        // Carga el perfil del user autenticado
        var profileOptional = profileRepository.findByUserId(userId);
        if (profileOptional.isEmpty())
            return ResponseEntity.notFound().build();

        Profile profile = profileOptional.get();
        profile.updateName(resource.firstName(), resource.lastName());
        profile.updateEmail(resource.email());
        profile.updatePhoneNumber(resource.phoneNumber());

        var savedProfile = profileRepository.save(profile);
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(savedProfile);

        return ResponseEntity.ok(profileResource);
    }

    @Operation(summary = "Get users by plan ID", description = "Retrieves all profiles/users that have a specific plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users with the specified plan found"),
            @ApiResponse(responseCode = "404", description = "No users found with the specified plan")
    })
    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<ProfileResource>> getUsersByPlanId(@PathVariable Long planId) {
        var getProfilesByPlanIdQuery = new GetProfilesByPlanIdQuery(planId);
        var profiles = profileQueryService.handle(getProfilesByPlanIdQuery);

        if (profiles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var profileResources = profiles.stream()
                .map(ProfileResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profileResources);
    }
}
